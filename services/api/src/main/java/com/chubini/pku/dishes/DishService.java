package com.chubini.pku.dishes;

import com.chubini.pku.dishes.dto.*;
import com.chubini.pku.products.Product;
import com.chubini.pku.products.ProductNotFoundException;
import com.chubini.pku.products.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DishService {

    private final DishRepository dishRepository;
    private final DishItemRepository dishItemRepository;
    private final ProductRepository productRepository;
    private final DishCalculator dishCalculator;

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public DishService(DishRepository dishRepository,
                      DishItemRepository dishItemRepository,
                      ProductRepository productRepository,
                      DishCalculator dishCalculator) {
        this.dishRepository = dishRepository;
        this.dishItemRepository = dishItemRepository;
        this.productRepository = productRepository;
        this.dishCalculator = dishCalculator;
    }

    /**
     * Create a new dish with items
     */
    public DishResponseDto createDish(DishCreateDto createDto) {
        // Create dish entity
        Dish dish = Dish.builder()
            .name(createDto.getName())
            .category(createDto.getCategory())
            .manualServingOverride(createDto.getManualServingGrams() != null)
            .build();

        // Create dish items
        List<DishItem> items = new ArrayList<>();
        for (DishCreateDto.DishItemCreateDto itemDto : createDto.getItems()) {
            UUID productId = UUID.fromString(itemDto.getProductId());
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

            DishItem item = DishItem.builder()
                .dish(dish)
                .product(product)
                .grams(itemDto.getGrams())
                .build();
            
            // Capture product snapshot
            item.captureProductSnapshot(product);
            items.add(item);
        }

        dish.setItems(items);

        // Calculate nutritional values
        recalculateDishNutrition(dish, createDto.getManualServingGrams());

        // Save dish (cascade will save items)
        Dish savedDish = dishRepository.save(dish);
        
        return convertToResponseDto(savedDish);
    }

    /**
     * Get dish by ID with all details
     */
    @Transactional(readOnly = true)
    public DishResponseDto getDishById(UUID dishId) {
        Dish dish = dishRepository.findByIdWithItems(dishId)
            .orElseThrow(() -> new DishNotFoundException(dishId));
        
        return convertToResponseDto(dish);
    }

    /**
     * Get all dishes with pagination and search
     */
    @Transactional(readOnly = true)
    public Page<DishResponseDto> getAllDishes(String query, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Dish> dishPage;

        if (category != null && !category.trim().isEmpty()) {
            if (query != null && !query.trim().isEmpty()) {
                dishPage = dishRepository.findByCategoryAndNameContainingIgnoreCase(category, query, pageable);
            } else {
                dishPage = dishRepository.findByCategory(category, pageable);
            }
        } else if (query != null && !query.trim().isEmpty()) {
            dishPage = dishRepository.findByNameContainingIgnoreCase(query, pageable);
        } else {
            dishPage = dishRepository.findAll(pageable);
        }

        return dishPage.map(this::convertToResponseDto);
    }

    /**
     * Scale dish to target grams
     */
    @Transactional(readOnly = true)
    public DishScaleResponseDto scaleDish(UUID dishId, DishScaleRequestDto scaleRequest) {
        Dish dish = dishRepository.findById(dishId)
            .orElseThrow(() -> new DishNotFoundException(dishId));

        BigDecimal originalGrams = dish.getNominalServingGrams();
        BigDecimal targetGrams = scaleRequest.getTargetGrams();
        BigDecimal scaleFactor = targetGrams.divide(originalGrams, SCALE, ROUNDING_MODE);

        // Create totals from current dish values
        DishCalculator.DishNutritionalTotals currentTotals = new DishCalculator.DishNutritionalTotals();
        currentTotals.totalPhenylalanine = dish.getTotalPhenylalanine();
        currentTotals.totalLeucine = dish.getTotalLeucine();
        currentTotals.totalTyrosine = dish.getTotalTyrosine();
        currentTotals.totalMethionine = dish.getTotalMethionine();
        currentTotals.totalKilojoules = dish.getTotalKilojoules();
        currentTotals.totalKilocalories = dish.getTotalKilocalories();
        currentTotals.totalProtein = dish.getTotalProtein();
        currentTotals.totalCarbohydrates = dish.getTotalCarbohydrates();
        currentTotals.totalFats = dish.getTotalFats();

        DishResponseDto.NutritionalValues scaledValues = dishCalculator.scaleValues(currentTotals, scaleFactor);

        DishScaleResponseDto response = new DishScaleResponseDto();
        response.setOriginalGrams(originalGrams);
        response.setTargetGrams(targetGrams);
        response.setScaleFactor(scaleFactor);
        response.setScaledValues(scaledValues);

        return response;
    }

    /**
     * Solve for required mass to achieve target phenylalanine (and optionally protein)
     */
    @Transactional(readOnly = true)
    public DishSolveMassResponseDto solveMass(UUID dishId, DishSolveMassRequestDto solveRequest) {
        Dish dish = dishRepository.findById(dishId)
            .orElseThrow(() -> new DishNotFoundException(dishId));

        BigDecimal targetPhenylalanine = solveRequest.getTargetPhenylalanine();
        BigDecimal targetProtein = solveRequest.getTargetProtein();

        if (dish.getPer100Phenylalanine().compareTo(BigDecimal.ZERO) == 0) {
            throw new DishCalculationException("Cannot solve for mass: dish has zero phenylalanine per 100g");
        }

        // Calculate required grams based on phenylalanine target (priority)
        BigDecimal requiredGrams = dishCalculator.calculateRequiredGramsForPhenylalanine(
            targetPhenylalanine, dish.getPer100Phenylalanine(), dish.getNominalServingGrams());

        // Calculate scale factor
        BigDecimal scaleFactor = requiredGrams.divide(dish.getNominalServingGrams(), SCALE, ROUNDING_MODE);

        // Calculate achieved values
        BigDecimal achievedPhenylalanine = dish.getPer100Phenylalanine()
            .multiply(requiredGrams)
            .divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE);

        BigDecimal achievedProtein = dish.getPer100Protein()
            .multiply(requiredGrams)
            .divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE);

        // Create complete nutritional profile
        DishCalculator.DishNutritionalTotals currentTotals = createTotalsFromDish(dish);
        DishResponseDto.NutritionalValues achievedValues = dishCalculator.scaleValues(currentTotals, scaleFactor);

        // Check if protein target is met (if provided)
        Boolean proteinTargetMet = null;
        String message = "Required grams calculated to achieve target phenylalanine.";
        
        if (targetProtein != null) {
            BigDecimal proteinDifference = achievedProtein.subtract(targetProtein).abs();
            BigDecimal tolerance = targetProtein.multiply(BigDecimal.valueOf(0.05)); // 5% tolerance
            proteinTargetMet = proteinDifference.compareTo(tolerance) <= 0;
            
            if (proteinTargetMet) {
                message = "Required grams calculated to achieve both phenylalanine and protein targets.";
            } else {
                message = String.format("Required grams calculated for phenylalanine target. " +
                    "Protein target not exactly met (achieved: %.2fg, target: %.2fg).",
                    achievedProtein, targetProtein);
            }
        }

        DishSolveMassResponseDto response = new DishSolveMassResponseDto();
        response.setRequiredGrams(requiredGrams);
        response.setScaleFactor(scaleFactor);
        response.setTargetPhenylalanine(targetPhenylalanine);
        response.setTargetProtein(targetProtein);
        response.setAchievedPhenylalanine(achievedPhenylalanine);
        response.setAchievedProtein(achievedProtein);
        response.setAchievedValues(achievedValues);
        response.setProteinTargetMet(proteinTargetMet);
        response.setMessage(message);

        return response;
    }

    /**
     * Update dish items (add, change, remove)
     */
    public DishResponseDto updateDishItems(UUID dishId, DishUpdateItemsDto updateDto) {
        Dish dish = dishRepository.findByIdWithItems(dishId)
            .orElseThrow(() -> new DishNotFoundException(dishId));

        // Clear existing items
        dish.clearItems();
        dishItemRepository.deleteByDishId(dishId);

        // Add new/updated items
        for (DishUpdateItemsDto.DishItemUpdateDto itemDto : updateDto.getItems()) {
            UUID productId = UUID.fromString(itemDto.getProductId());
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

            DishItem item = DishItem.builder()
                .dish(dish)
                .product(product)
                .grams(itemDto.getGrams())
                .build();

            // Capture product snapshot
            item.captureProductSnapshot(product);
            dish.addItem(item);
        }

        // Recalculate nutrition (preserve manual serving override if set)
        BigDecimal manualServing = dish.getManualServingOverride() ? dish.getNominalServingGrams() : null;
        recalculateDishNutrition(dish, manualServing);

        Dish savedDish = dishRepository.save(dish);
        return convertToResponseDto(savedDish);
    }

    /**
     * Get dishes with low phenylalanine
     */
    @Transactional(readOnly = true)
    public Page<DishResponseDto> getLowPheDishes(Double maxPhe, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("per100Phenylalanine"));
        Page<Dish> dishPage = dishRepository.findByLowPhenylalanine(BigDecimal.valueOf(maxPhe), pageable);
        return dishPage.map(this::convertToResponseDto);
    }

    /**
     * Get all dish categories
     */
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return dishRepository.findAllCategories();
    }

    /**
     * Delete dish
     */
    public void deleteDish(UUID dishId) {
        if (!dishRepository.existsById(dishId)) {
            throw new DishNotFoundException(dishId);
        }
        dishRepository.deleteById(dishId);
    }

    /**
     * Recalculate dish nutrition based on current items
     */
    private void recalculateDishNutrition(Dish dish, BigDecimal manualServingGrams) {
        DishCalculator.DishNutritionalTotals totals = dishCalculator.calculateTotals(dish.getItems());

        // Set nominal serving grams
        if (manualServingGrams != null) {
            dish.setNominalServingGrams(manualServingGrams);
            dish.setManualServingOverride(true);
        } else {
            dish.setNominalServingGrams(totals.totalGrams);
            dish.setManualServingOverride(false);
        }

        // Set total values
        dish.setTotalPhenylalanine(totals.totalPhenylalanine);
        dish.setTotalLeucine(totals.totalLeucine);
        dish.setTotalTyrosine(totals.totalTyrosine);
        dish.setTotalMethionine(totals.totalMethionine);
        dish.setTotalKilojoules(totals.totalKilojoules);
        dish.setTotalKilocalories(totals.totalKilocalories);
        dish.setTotalProtein(totals.totalProtein);
        dish.setTotalCarbohydrates(totals.totalCarbohydrates);
        dish.setTotalFats(totals.totalFats);

        // Calculate and set per 100g values
        DishCalculator.DishNutritionalTotals per100Values = dishCalculator.calculatePer100Values(totals, dish.getNominalServingGrams());
        dish.setPer100Phenylalanine(per100Values.totalPhenylalanine);
        dish.setPer100Leucine(per100Values.totalLeucine);
        dish.setPer100Tyrosine(per100Values.totalTyrosine);
        dish.setPer100Methionine(per100Values.totalMethionine);
        dish.setPer100Kilojoules(per100Values.totalKilojoules);
        dish.setPer100Kilocalories(per100Values.totalKilocalories);
        dish.setPer100Protein(per100Values.totalProtein);
        dish.setPer100Carbohydrates(per100Values.totalCarbohydrates);
        dish.setPer100Fats(per100Values.totalFats);
    }

    /**
     * Convert Dish entity to response DTO
     */
    private DishResponseDto convertToResponseDto(Dish dish) {
        DishResponseDto dto = new DishResponseDto();
        dto.setId(dish.getId());
        dto.setName(dish.getName());
        dto.setCategory(dish.getCategory());
        dto.setNominalServingGrams(dish.getNominalServingGrams());
        dto.setManualServingOverride(dish.getManualServingOverride());
        dto.setCreatedAt(dish.getCreatedAt());
        dto.setUpdatedAt(dish.getUpdatedAt());

        // Set total values
        DishResponseDto.NutritionalValues totalValues = new DishResponseDto.NutritionalValues();
        totalValues.setPhenylalanine(dish.getTotalPhenylalanine());
        totalValues.setLeucine(dish.getTotalLeucine());
        totalValues.setTyrosine(dish.getTotalTyrosine());
        totalValues.setMethionine(dish.getTotalMethionine());
        totalValues.setKilojoules(dish.getTotalKilojoules());
        totalValues.setKilocalories(dish.getTotalKilocalories());
        totalValues.setProtein(dish.getTotalProtein());
        totalValues.setCarbohydrates(dish.getTotalCarbohydrates());
        totalValues.setFats(dish.getTotalFats());
        dto.setTotalValues(totalValues);

        // Set per 100g values
        DishResponseDto.NutritionalValues per100Values = new DishResponseDto.NutritionalValues();
        per100Values.setPhenylalanine(dish.getPer100Phenylalanine());
        per100Values.setLeucine(dish.getPer100Leucine());
        per100Values.setTyrosine(dish.getPer100Tyrosine());
        per100Values.setMethionine(dish.getPer100Methionine());
        per100Values.setKilojoules(dish.getPer100Kilojoules());
        per100Values.setKilocalories(dish.getPer100Kilocalories());
        per100Values.setProtein(dish.getPer100Protein());
        per100Values.setCarbohydrates(dish.getPer100Carbohydrates());
        per100Values.setFats(dish.getPer100Fats());
        dto.setPer100Values(per100Values);

        // Set items
        List<DishResponseDto.DishItemResponseDto> itemDtos = dish.getItems().stream()
            .map(this::convertToItemResponseDto)
            .collect(Collectors.toList());
        dto.setItems(itemDtos);

        return dto;
    }

    /**
     * Convert DishItem entity to response DTO
     */
    private DishResponseDto.DishItemResponseDto convertToItemResponseDto(DishItem item) {
        DishResponseDto.DishItemResponseDto dto = new DishResponseDto.DishItemResponseDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getProductName());
        dto.setProductCategory(item.getProduct().getCategory());
        dto.setGrams(item.getGrams());
        dto.setCreatedAt(item.getCreatedAt());
        
        // Calculate contribution
        dto.setContribution(dishCalculator.calculateItemContribution(item));
        
        return dto;
    }

    /**
     * Helper method to create totals from dish entity
     */
    private DishCalculator.DishNutritionalTotals createTotalsFromDish(Dish dish) {
        DishCalculator.DishNutritionalTotals totals = new DishCalculator.DishNutritionalTotals();
        totals.totalPhenylalanine = dish.getTotalPhenylalanine();
        totals.totalLeucine = dish.getTotalLeucine();
        totals.totalTyrosine = dish.getTotalTyrosine();
        totals.totalMethionine = dish.getTotalMethionine();
        totals.totalKilojoules = dish.getTotalKilojoules();
        totals.totalKilocalories = dish.getTotalKilocalories();
        totals.totalProtein = dish.getTotalProtein();
        totals.totalCarbohydrates = dish.getTotalCarbohydrates();
        totals.totalFats = dish.getTotalFats();
        return totals;
    }
}
