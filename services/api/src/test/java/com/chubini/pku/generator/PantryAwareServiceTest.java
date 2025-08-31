package com.chubini.pku.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import com.chubini.pku.pantry.PantryItem;
import com.chubini.pku.pantry.PantryRepository;
import com.chubini.pku.pantry.PriceEntry;
import com.chubini.pku.pantry.PriceRepository;
import com.chubini.pku.patients.PatientProfile;
import com.chubini.pku.products.Product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PantryAwareServiceTest {

  @Mock private PantryRepository pantryRepository;

  @Mock private PriceRepository priceRepository;

  @InjectMocks private PantryAwareService pantryAwareService;

  private PatientProfile testPatient;
  private Product testProduct;

  @BeforeEach
  void setUp() {
    testPatient = PatientProfile.builder().id(UUID.randomUUID()).name("Test Patient").build();

    testProduct = Product.builder().id(UUID.randomUUID()).productName("Test Product").build();
  }

  @Test
  void testCheckPantryAvailability_ItemExists_ReturnsAvailable() {
    // Given: product is available in pantry
    PantryItem pantryItem =
        PantryItem.builder()
            .id(UUID.randomUUID())
            .patient(testPatient)
            .product(testProduct)
            .itemType(PantryItem.ItemType.PRODUCT)
            .quantityGrams(BigDecimal.valueOf(500))
            .isAvailable(true)
            .costPerUnit(BigDecimal.valueOf(5.00))
            .build();

    when(pantryRepository.findByPatientAndProductAndIsAvailableTrueOrderByExpiryDateAsc(
            testPatient, testProduct))
        .thenReturn(Arrays.asList(pantryItem));

    // When
    PantryAwareService.PantryAvailability availability =
        pantryAwareService.checkPantryAvailability(
            testProduct, testPatient, BigDecimal.valueOf(100));

    // Then
    assertThat(availability.isAvailable()).isTrue();
    assertThat(availability.isSufficient()).isTrue();
    assertThat(availability.getTotalQuantityAvailable())
        .isEqualByComparingTo(BigDecimal.valueOf(500));
  }

  @Test
  void testCheckPantryAvailability_ItemNotExists_ReturnsNotAvailable() {
    // Given: product is not in pantry
    when(pantryRepository.findByPatientAndProductAndIsAvailableTrueOrderByExpiryDateAsc(
            testPatient, testProduct))
        .thenReturn(Collections.emptyList());

    // When
    PantryAwareService.PantryAvailability availability =
        pantryAwareService.checkPantryAvailability(
            testProduct, testPatient, BigDecimal.valueOf(100));

    // Then
    assertThat(availability.isAvailable()).isFalse();
    assertThat(availability.isSufficient()).isFalse();
    assertThat(availability.getTotalQuantityAvailable()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void testCheckPantryAvailability_InsufficientQuantity_ReturnsInsufficientButAvailable() {
    // Given: product is available but in insufficient quantity
    PantryItem pantryItem =
        PantryItem.builder()
            .id(UUID.randomUUID())
            .patient(testPatient)
            .product(testProduct)
            .itemType(PantryItem.ItemType.PRODUCT)
            .quantityGrams(BigDecimal.valueOf(50)) // Less than required 100
            .isAvailable(true)
            .build();

    when(pantryRepository.findByPatientAndProductAndIsAvailableTrueOrderByExpiryDateAsc(
            testPatient, testProduct))
        .thenReturn(Arrays.asList(pantryItem));

    // When
    PantryAwareService.PantryAvailability availability =
        pantryAwareService.checkPantryAvailability(
            testProduct, testPatient, BigDecimal.valueOf(100));

    // Then
    assertThat(availability.isAvailable()).isTrue();
    assertThat(availability.isSufficient()).isFalse();
    assertThat(availability.getTotalQuantityAvailable())
        .isEqualByComparingTo(BigDecimal.valueOf(50));
  }

  @Test
  void testReservePantryQuantity_SufficientStock_ReservesSuccessfully() {
    // Given: sufficient stock in pantry
    PantryItem pantryItem =
        PantryItem.builder().id(UUID.randomUUID()).quantityGrams(BigDecimal.valueOf(500)).build();

    List<PantryItem> pantryItems = Arrays.asList(pantryItem);

    // When
    boolean reserved =
        pantryAwareService.reservePantryQuantity(pantryItems, BigDecimal.valueOf(100));

    // Then
    assertThat(reserved).isTrue();
  }

  @Test
  void testReservePantryQuantity_InsufficientStock_ReservationFails() {
    // Given: insufficient stock in pantry
    PantryItem pantryItem =
        PantryItem.builder()
            .id(UUID.randomUUID())
            .quantityGrams(BigDecimal.valueOf(50)) // Less than required 100
            .build();

    List<PantryItem> pantryItems = Arrays.asList(pantryItem);

    // When
    boolean reserved =
        pantryAwareService.reservePantryQuantity(pantryItems, BigDecimal.valueOf(100));

    // Then
    assertThat(reserved).isFalse();
  }

  @Test
  void testClearPantryReservations_ClearsInMemoryReservations() {
    // Given: some reservations have been made
    PantryItem pantryItem =
        PantryItem.builder().id(UUID.randomUUID()).quantityGrams(BigDecimal.valueOf(500)).build();

    pantryAwareService.reservePantryQuantity(Arrays.asList(pantryItem), BigDecimal.valueOf(100));

    // When
    pantryAwareService.clearPantryReservations();

    // Then - should be able to reserve again after clearing
    boolean reserved =
        pantryAwareService.reservePantryQuantity(
            Arrays.asList(pantryItem), BigDecimal.valueOf(100));
    assertThat(reserved).isTrue();
  }

  @Test
  void testGetCurrentCost_WithPantryItem_ReturnsPantryCost() {
    // Given: product is in pantry with cost
    PantryItem pantryItem =
        PantryItem.builder()
            .id(UUID.randomUUID())
            .patient(testPatient)
            .product(testProduct)
            .quantityGrams(BigDecimal.valueOf(500))
            .isAvailable(true)
            .costPerUnit(BigDecimal.valueOf(10.00))
            .build();

    when(pantryRepository.findByPatientAndProductAndIsAvailableTrueOrderByExpiryDateAsc(
            testPatient, testProduct))
        .thenReturn(Arrays.asList(pantryItem));

    // When
    BigDecimal cost =
        pantryAwareService.getCurrentCost(testProduct, BigDecimal.valueOf(100), testPatient);

    // Then
    assertThat(cost).isGreaterThan(BigDecimal.ZERO);
  }

  @Test
  void testGetCurrentCost_WithoutPantryItem_ReturnsMarketPrice() {
    // Given: product is not in pantry but has market price
    when(pantryRepository.findByPatientAndProductAndIsAvailableTrueOrderByExpiryDateAsc(
            testPatient, testProduct))
        .thenReturn(Collections.emptyList());

    PriceEntry priceEntry =
        PriceEntry.builder()
            .product(testProduct)
            .itemType(PantryItem.ItemType.PRODUCT)
            .pricePerUnit(BigDecimal.valueOf(1.00))
            .unitSizeGrams(BigDecimal.valueOf(100))
            .build();

    when(priceRepository.findBestPriceForProduct(testProduct)).thenReturn(Optional.of(priceEntry));

    // When
    BigDecimal cost =
        pantryAwareService.getCurrentCost(testProduct, BigDecimal.valueOf(100), testPatient);

    // Then
    assertThat(cost).isEqualByComparingTo(BigDecimal.valueOf(10.00)); // 100 * 0.10
  }

  @Test
  void testGetCurrentCost_NoData_ReturnsDefaultEstimate() {
    // Given: no pantry item and no market price
    when(pantryRepository.findByPatientAndProductAndIsAvailableTrueOrderByExpiryDateAsc(
            testPatient, testProduct))
        .thenReturn(Collections.emptyList());
    when(priceRepository.findBestPriceForProduct(testProduct)).thenReturn(Optional.empty());

    // When
    BigDecimal cost =
        pantryAwareService.getCurrentCost(testProduct, BigDecimal.valueOf(100), testPatient);

    // Then
    assertThat(cost).isEqualByComparingTo(BigDecimal.valueOf(5.00)); // 100 * 0.05 default
  }

  @Test
  void testGetExpiringSoonItems_ReturnsExpiringSoonItems() {
    // Given: patient has items expiring soon
    List<PantryItem> expiringSoonItems =
        Arrays.asList(
            PantryItem.builder()
                .id(UUID.randomUUID())
                .patient(testPatient)
                .expiryDate(LocalDate.now().plusDays(2))
                .isAvailable(true)
                .build());

    when(pantryRepository.findExpiringSoon(
            eq(testPatient), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(expiringSoonItems);

    // When
    List<PantryItem> result = pantryAwareService.getExpiringSoonItems(testPatient, 3);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getPatient()).isEqualTo(testPatient);
  }

  @Test
  void testGetExpiringSoonItems_NoExpiringSoonItems_ReturnsEmptyList() {
    // Given: patient has no items expiring soon
    when(pantryRepository.findExpiringSoon(
            eq(testPatient), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(Collections.emptyList());

    // When
    List<PantryItem> result = pantryAwareService.getExpiringSoonItems(testPatient, 3);

    // Then
    assertThat(result).isEmpty();
  }
}
