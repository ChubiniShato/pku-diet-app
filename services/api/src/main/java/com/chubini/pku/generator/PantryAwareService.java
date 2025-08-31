package com.chubini.pku.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.chubini.pku.generator.dto.FoodCandidate;
import com.chubini.pku.pantry.PantryItem;
import com.chubini.pku.pantry.PantryRepository;
import com.chubini.pku.pantry.PriceEntry;
import com.chubini.pku.pantry.PriceRepository;
import com.chubini.pku.patients.PatientProfile;
import com.chubini.pku.products.CustomProduct;
import com.chubini.pku.products.Product;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Service for pantry-aware food selection and cost calculation */
@Service
@RequiredArgsConstructor
@Slf4j
public class PantryAwareService {

  private final PantryRepository pantryRepository;
  private final PriceRepository priceRepository;

  // In-memory simulation of pantry reservations during menu generation
  private final Map<UUID, BigDecimal> pantryReservations = new ConcurrentHashMap<>();

  /** Check pantry availability for a product */
  public PantryAvailability checkPantryAvailability(
      Product product, PatientProfile patient, BigDecimal requiredQuantity) {
    if (product == null || patient == null || requiredQuantity == null) {
      return PantryAvailability.notAvailable();
    }

    List<PantryItem> pantryItems =
        pantryRepository.findByPatientAndProductAndIsAvailableTrueOrderByExpiryDateAsc(
            patient, product);
    return calculateAvailability(pantryItems, requiredQuantity);
  }

  /** Check pantry availability for a custom product */
  public PantryAvailability checkPantryAvailability(
      CustomProduct customProduct, PatientProfile patient, BigDecimal requiredQuantity) {
    if (customProduct == null || patient == null || requiredQuantity == null) {
      return PantryAvailability.notAvailable();
    }

    List<PantryItem> pantryItems =
        pantryRepository.findByPatientAndCustomProductAndIsAvailableTrueOrderByExpiryDateAsc(
            patient, customProduct);
    return calculateAvailability(pantryItems, requiredQuantity);
  }

  /** Calculate availability from pantry items */
  private PantryAvailability calculateAvailability(
      List<PantryItem> pantryItems, BigDecimal requiredQuantity) {
    if (pantryItems.isEmpty()) {
      return PantryAvailability.notAvailable();
    }

    BigDecimal totalAvailable = BigDecimal.ZERO;
    BigDecimal totalCost = BigDecimal.ZERO;
    List<PantryItem> usableItems = new ArrayList<>();

    // Consider reserved quantities
    for (PantryItem item : pantryItems) {
      BigDecimal reserved = pantryReservations.getOrDefault(item.getId(), BigDecimal.ZERO);
      BigDecimal availableQuantity = item.getQuantityGrams().subtract(reserved);

      if (availableQuantity.compareTo(BigDecimal.ZERO) > 0) {
        usableItems.add(item);
        totalAvailable = totalAvailable.add(availableQuantity);

        // Add to cost calculation if cost is available
        if (item.getCostPerUnit() != null) {
          BigDecimal itemCost =
              item.getCostPerGram()
                  .multiply(
                      availableQuantity.min(
                          requiredQuantity.subtract(
                              totalCost.divide(
                                  item.getCostPerGram().compareTo(BigDecimal.ZERO) > 0
                                      ? item.getCostPerGram()
                                      : BigDecimal.ONE,
                                  2,
                                  RoundingMode.HALF_UP))));
          totalCost = totalCost.add(itemCost);
        }
      }
    }

    boolean sufficient = totalAvailable.compareTo(requiredQuantity) >= 0;
    boolean expiringSoon = usableItems.stream().anyMatch(PantryItem::isExpiringSoon);

    return PantryAvailability.builder()
        .available(true)
        .sufficient(sufficient)
        .totalQuantityAvailable(totalAvailable)
        .estimatedCost(totalCost)
        .pantryItems(usableItems)
        .expiringSoon(expiringSoon)
        .build();
  }

  /** Reserve pantry quantity for menu generation simulation */
  public boolean reservePantryQuantity(List<PantryItem> pantryItems, BigDecimal quantity) {
    if (pantryItems.isEmpty() || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      return false;
    }

    BigDecimal remainingToReserve = quantity;
    Map<UUID, BigDecimal> newReservations = new HashMap<>();

    for (PantryItem item : pantryItems) {
      if (remainingToReserve.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }

      BigDecimal currentReserved = pantryReservations.getOrDefault(item.getId(), BigDecimal.ZERO);
      BigDecimal availableInItem = item.getQuantityGrams().subtract(currentReserved);

      if (availableInItem.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal toReserveFromItem = remainingToReserve.min(availableInItem);
        newReservations.put(item.getId(), currentReserved.add(toReserveFromItem));
        remainingToReserve = remainingToReserve.subtract(toReserveFromItem);
      }
    }

    // Apply reservations if we can reserve the full quantity
    boolean canReserveAll = remainingToReserve.compareTo(BigDecimal.ZERO) <= 0;
    if (canReserveAll) {
      pantryReservations.putAll(newReservations);
      log.debug("Reserved {} grams from {} pantry items", quantity, newReservations.size());
    }

    return canReserveAll;
  }

  /** Clear all pantry reservations (call after menu generation) */
  public void clearPantryReservations() {
    int clearedCount = pantryReservations.size();
    pantryReservations.clear();
    log.debug("Cleared {} pantry reservations", clearedCount);
  }

  /** Get current cost for a product (from price database or pantry cost) */
  public BigDecimal getCurrentCost(Product product, BigDecimal quantity, PatientProfile patient) {
    if (product == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    // First check if we have it in pantry
    PantryAvailability pantryAvail = checkPantryAvailability(product, patient, quantity);
    if (pantryAvail.isSufficient() && pantryAvail.getEstimatedCost() != null) {
      return pantryAvail.getEstimatedCost();
    }

    // Fall back to market price
    Optional<PriceEntry> bestPrice = priceRepository.findBestPriceForProduct(product);
    if (bestPrice.isPresent()) {
      return bestPrice.get().getPricePerGram().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    // Default estimate if no price data
    return quantity.multiply(new BigDecimal("0.05")); // 5 cents per gram default
  }

  /** Get current cost for a custom product */
  public BigDecimal getCurrentCost(
      CustomProduct customProduct, BigDecimal quantity, PatientProfile patient) {
    if (customProduct == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    // First check if we have it in pantry
    PantryAvailability pantryAvail = checkPantryAvailability(customProduct, patient, quantity);
    if (pantryAvail.isSufficient() && pantryAvail.getEstimatedCost() != null) {
      return pantryAvail.getEstimatedCost();
    }

    // Fall back to market price
    Optional<PriceEntry> bestPrice = priceRepository.findBestPriceForCustomProduct(customProduct);
    if (bestPrice.isPresent()) {
      return bestPrice.get().getPricePerGram().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    // Default estimate if no price data
    return quantity.multiply(new BigDecimal("0.05")); // 5 cents per gram default
  }

  /** Enhance food candidate with pantry and cost information */
  public void enhanceCandidateWithPantryInfo(FoodCandidate candidate, PatientProfile patient) {
    if (candidate == null || patient == null) {
      return;
    }

    BigDecimal requiredQuantity = candidate.getSuggestedServingGrams();
    if (requiredQuantity == null) {
      return;
    }

    PantryAvailability availability;
    BigDecimal cost;

    switch (candidate.getEntryType()) {
      case PRODUCT -> {
        availability = checkPantryAvailability(candidate.getProduct(), patient, requiredQuantity);
        cost = getCurrentCost(candidate.getProduct(), requiredQuantity, patient);
      }
      case CUSTOM_PRODUCT -> {
        availability =
            checkPantryAvailability(candidate.getCustomProduct(), patient, requiredQuantity);
        cost = getCurrentCost(candidate.getCustomProduct(), requiredQuantity, patient);
      }
      default -> {
        availability = PantryAvailability.notAvailable();
        cost = requiredQuantity.multiply(new BigDecimal("0.05")); // Default estimate
      }
    }

    candidate.setAvailableInPantry(availability.isAvailable());
    candidate.setPantryQuantityAvailable(availability.getTotalQuantityAvailable());
    candidate.setPantryItems(availability.getPantryItems());
    candidate.setCostPerServing(cost);

    log.debug(
        "Enhanced candidate {}: pantry={}, cost={}",
        candidate.getItemName(),
        availability.isAvailable(),
        cost);
  }

  /** Get items expiring soon that should be prioritized */
  public List<PantryItem> getExpiringSoonItems(PatientProfile patient, int daysAhead) {
    LocalDate cutoffDate = LocalDate.now().plusDays(daysAhead);
    return pantryRepository.findExpiringSoon(patient, LocalDate.now(), cutoffDate);
  }

  /** Find cheaper alternatives from pantry or market */
  public List<FoodCandidate> findCheaperAlternatives(
      String category, PatientProfile patient, BigDecimal maxCost) {
    // This would be enhanced to search through available products and pantry items
    // For now, return empty list as this would require complex product matching logic
    return new ArrayList<>();
  }

  /** Data class for pantry availability information */
  public static class PantryAvailability {
    private final boolean available;
    private final boolean sufficient;
    private final BigDecimal totalQuantityAvailable;
    private final BigDecimal estimatedCost;
    private final List<PantryItem> pantryItems;
    private final boolean expiringSoon;

    @lombok.Builder
    public PantryAvailability(
        boolean available,
        boolean sufficient,
        BigDecimal totalQuantityAvailable,
        BigDecimal estimatedCost,
        List<PantryItem> pantryItems,
        boolean expiringSoon) {
      this.available = available;
      this.sufficient = sufficient;
      this.totalQuantityAvailable = totalQuantityAvailable;
      this.estimatedCost = estimatedCost;
      this.pantryItems = pantryItems != null ? pantryItems : new ArrayList<>();
      this.expiringSoon = expiringSoon;
    }

    public static PantryAvailability notAvailable() {
      return PantryAvailability.builder()
          .available(false)
          .sufficient(false)
          .totalQuantityAvailable(BigDecimal.ZERO)
          .estimatedCost(BigDecimal.ZERO)
          .pantryItems(new ArrayList<>())
          .expiringSoon(false)
          .build();
    }

    // Getters
    public boolean isAvailable() {
      return available;
    }

    public boolean isSufficient() {
      return sufficient;
    }

    public BigDecimal getTotalQuantityAvailable() {
      return totalQuantityAvailable;
    }

    public BigDecimal getEstimatedCost() {
      return estimatedCost;
    }

    public List<PantryItem> getPantryItems() {
      return pantryItems;
    }

    public boolean isExpiringSoon() {
      return expiringSoon;
    }
  }
}
