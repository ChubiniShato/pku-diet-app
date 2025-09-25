package com.chubini.pku.dishes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chubini.pku.products.Product;
import com.chubini.pku.products.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DishService {

  private final DishRepository dishRepository;
  private final ProductRepository productRepository;

  public Page<Dish> getAllDishes(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return dishRepository.findByIsVisibleTrueOrderByName(pageable);
  }

  public Page<Dish> getDishesByCategory(String category, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return dishRepository.findByCategoryAndIsVisibleTrue(category, pageable);
  }

  public List<String> getAllCategories() {
    return dishRepository.findDistinctCategories();
  }

  public Optional<Dish> getDishById(UUID id) {
    return dishRepository.findById(id);
  }

  public Page<Dish> searchDishes(String searchTerm, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return dishRepository.findBySearchTerm(searchTerm, pageable);
  }

  public Page<Dish> getLowPheDishes(Double maxPhe, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return dishRepository.findLowPheDishes(maxPhe, pageable);
  }

  @Transactional
  public Dish createDish(Dish dish) {
    // Calculate nutrition values if ingredients are provided
    if (dish.getIngredients() != null && !dish.getIngredients().isEmpty()) {
      calculateNutritionValues(dish);
    }

    return dishRepository.save(dish);
  }

  @Transactional
  public Dish updateDish(UUID id, Dish dishDetails) {
    Dish dish =
        dishRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Dish not found with id: " + id));

    // Update basic fields
    dish.setName(dishDetails.getName());
    dish.setCategory(dishDetails.getCategory());
    dish.setDescription(dishDetails.getDescription());
    dish.setNominalServingGrams(dishDetails.getNominalServingGrams());
    dish.setManualServingOverride(dishDetails.getManualServingOverride());
    dish.setPreparationTimeMinutes(dishDetails.getPreparationTimeMinutes());
    dish.setDifficultyLevel(dishDetails.getDifficultyLevel());
    dish.setRecipeInstructions(dishDetails.getRecipeInstructions());
    dish.setIsVerified(dishDetails.getIsVerified());

    // Update ingredients if provided
    if (dishDetails.getIngredients() != null) {
      dish.getIngredients().clear();
      dish.getIngredients().addAll(dishDetails.getIngredients());
      // Recalculate nutrition values
      calculateNutritionValues(dish);
    }

    return dishRepository.save(dish);
  }

  @Transactional
  public void deleteDish(UUID id) {
    Dish dish =
        dishRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Dish not found with id: " + id));

    // Soft delete - mark as invisible
    dish.setIsVisible(false);
    dishRepository.save(dish);
  }

  /** Calculate nutritional values for a dish based on its ingredients */
  private void calculateNutritionValues(Dish dish) {
    BigDecimal ingredientsWeight = BigDecimal.ZERO;
    BigDecimal totalPhenylalanine = BigDecimal.ZERO;
    BigDecimal totalLeucine = BigDecimal.ZERO;
    BigDecimal totalTyrosine = BigDecimal.ZERO;
    BigDecimal totalMethionine = BigDecimal.ZERO;
    BigDecimal totalKilojoules = BigDecimal.ZERO;
    BigDecimal totalKilocalories = BigDecimal.ZERO;
    BigDecimal totalProtein = BigDecimal.ZERO;
    BigDecimal totalCarbohydrates = BigDecimal.ZERO;
    BigDecimal totalFats = BigDecimal.ZERO;

    // Calculate nutrition from ingredients
    for (DishIngredient ingredient : dish.getIngredients()) {
      Product product = ingredient.getProduct();
      BigDecimal ingredientWeight = ingredient.getQuantityGrams();

      ingredientsWeight = ingredientsWeight.add(ingredientWeight);

      // Calculate nutrition per ingredient based on weight (per 100g product values)
      BigDecimal weightRatio =
          ingredientWeight.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);

      if (product.getPhenylalanine() != null) {
        totalPhenylalanine =
            totalPhenylalanine.add(product.getPhenylalanine().multiply(weightRatio));
      }
      if (product.getLeucine() != null) {
        totalLeucine = totalLeucine.add(product.getLeucine().multiply(weightRatio));
      }
      if (product.getTyrosine() != null) {
        totalTyrosine = totalTyrosine.add(product.getTyrosine().multiply(weightRatio));
      }
      if (product.getMethionine() != null) {
        totalMethionine = totalMethionine.add(product.getMethionine().multiply(weightRatio));
      }
      if (product.getKilojoules() != null) {
        totalKilojoules = totalKilojoules.add(product.getKilojoules().multiply(weightRatio));
      }
      if (product.getKilocalories() != null) {
        totalKilocalories = totalKilocalories.add(product.getKilocalories().multiply(weightRatio));
      }
      if (product.getProtein() != null) {
        totalProtein = totalProtein.add(product.getProtein().multiply(weightRatio));
      }
      if (product.getCarbohydrates() != null) {
        totalCarbohydrates =
            totalCarbohydrates.add(product.getCarbohydrates().multiply(weightRatio));
      }
      if (product.getFats() != null) {
        totalFats = totalFats.add(product.getFats().multiply(weightRatio));
      }
    }

    // Set total nutrition values (for the entire dish)
    dish.setTotalPhenylalanine(totalPhenylalanine);
    dish.setTotalLeucine(totalLeucine);
    dish.setTotalTyrosine(totalTyrosine);
    dish.setTotalMethionine(totalMethionine);
    dish.setTotalKilojoules(totalKilojoules);
    dish.setTotalKilocalories(totalKilocalories);
    dish.setTotalProtein(totalProtein);
    dish.setTotalCarbohydrates(totalCarbohydrates);
    dish.setTotalFats(totalFats);

    // Calculate per 100g values based on FINAL DISH WEIGHT (not ingredients weight)
    BigDecimal finalDishWeight = dish.getNominalServingGrams();
    if (finalDishWeight != null && finalDishWeight.compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal per100Ratio =
          BigDecimal.valueOf(100).divide(finalDishWeight, 4, java.math.RoundingMode.HALF_UP);

      dish.setPer100Phenylalanine(totalPhenylalanine.multiply(per100Ratio));
      dish.setPer100Leucine(totalLeucine.multiply(per100Ratio));
      dish.setPer100Tyrosine(totalTyrosine.multiply(per100Ratio));
      dish.setPer100Methionine(totalMethionine.multiply(per100Ratio));
      dish.setPer100Kilojoules(totalKilojoules.multiply(per100Ratio));
      dish.setPer100Kilocalories(totalKilocalories.multiply(per100Ratio));
      dish.setPer100Protein(totalProtein.multiply(per100Ratio));
      dish.setPer100Carbohydrates(totalCarbohydrates.multiply(per100Ratio));
      dish.setPer100Fats(totalFats.multiply(per100Ratio));
    }

    // Calculate water content for information
    BigDecimal waterContent = finalDishWeight.subtract(ingredientsWeight);
    System.out.println(
        "Dish: "
            + dish.getName()
            + " | Ingredients: "
            + ingredientsWeight
            + "g"
            + " | Final weight: "
            + finalDishWeight
            + "g"
            + " | Water added: "
            + waterContent
            + "g");
  }

  /** Find product by name (for ingredient matching) */
  public Optional<Product> findProductByName(String name) {
    // Try exact match first
    List<Product> products = productRepository.findAll();
    for (Product product : products) {
      if (product.getProductName().equalsIgnoreCase(name.trim())) {
        return Optional.of(product);
      }
    }

    // Try partial match
    for (Product product : products) {
      if (product.getProductName().toLowerCase().contains(name.toLowerCase().trim())) {
        return Optional.of(product);
      }
    }

    return Optional.empty();
  }

  /** Generate CSV template with all available products for dish creation */
  public String generateCsvTemplate() {
    List<Product> products = productRepository.findAll();

    StringBuilder csvTemplate = new StringBuilder();

    // CSV Header
    csvTemplate.append(
        "dish_name,category,final_dish_weight,ingredient_1,ingredient_1_grams,ingredient_2,ingredient_2_grams,ingredient_3,ingredient_3_grams,ingredient_4,ingredient_4_grams,ingredient_5,ingredient_5_grams,ingredient_6,ingredient_6_grams,preparation_time_minutes,difficulty_level,recipe_instructions\n");

    // Example row with available products as comments
    csvTemplate.append(
        "# Example: Vegetable Soup,Soups,200,Potato,50,Carrot,30,Onion,20,,,,,,,30,EASY,\"1. Cut vegetables. 2. Cook in water. 3. Season to taste.\"\n");
    csvTemplate.append("# \n");
    csvTemplate.append("# Available Products (use exact names):\n");

    products.stream()
        .filter(p -> p.getProductName() != null && !p.getProductName().trim().isEmpty())
        .sorted(
            (p1, p2) -> {
              String cat1 = p1.getCategory() != null ? p1.getCategory() : "Other";
              String cat2 = p2.getCategory() != null ? p2.getCategory() : "Other";
              int catCompare = cat1.compareTo(cat2);
              if (catCompare != 0) return catCompare;
              return p1.getProductName().compareTo(p2.getProductName());
            })
        .forEach(
            product -> {
              String category = product.getCategory() != null ? product.getCategory() : "Other";
              csvTemplate
                  .append("# ")
                  .append(category)
                  .append(": ")
                  .append(product.getProductName())
                  .append("\n");
            });

    csvTemplate.append("# \n");
    csvTemplate.append("# Instructions:\n");
    csvTemplate.append("# - Use exact product names from the list above\n");
    csvTemplate.append("# - final_dish_weight: Total weight of finished dish in grams\n");
    csvTemplate.append("# - ingredient_X_grams: Weight of each ingredient in grams\n");
    csvTemplate.append("# - difficulty_level: EASY, MEDIUM, or HARD\n");
    csvTemplate.append("# - Leave unused ingredient columns empty\n");
    csvTemplate.append("# \n");
    csvTemplate.append("# Template rows (remove # and fill in your data):\n");
    csvTemplate.append(
        "# My Dish Name,Soups,250,Potato,100,Carrot,50,Onion,25,,,,,,,45,MEDIUM,\"Cooking instructions here\"\n");

    return csvTemplate.toString();
  }

  /** Generate multi-language CSV template for dish creation */
  public String generateMultiLanguageCsvTemplate() {
    List<Product> products = productRepository.findAll();

    StringBuilder csvTemplate = new StringBuilder();

    // CSV Header for multi-language support
    csvTemplate.append(
        "dish_name_ka,dish_name_ru,dish_name_en,category_ka,category_ru,category_en,final_dish_weight,ingredient_1,ingredient_1_grams,ingredient_2,ingredient_2_grams,ingredient_3,ingredient_3_grams,ingredient_4,ingredient_4_grams,ingredient_5,ingredient_5_grams,ingredient_6,ingredient_6_grams,preparation_time_minutes,difficulty_level,recipe_instructions_ka,recipe_instructions_ru,recipe_instructions_en\n");

    // Example rows with multi-language support
    csvTemplate.append("# Example recipes:\n");
    csvTemplate.append(
        "ბოსტნეული წვნიანი,Овощной суп,Vegetable Soup,წვნიანები,Супы,Soups,200,Potato,50,Carrot,30,Onion,20,,,,,,,30,EASY,\"1. დაჭერი კარტოფილი. 2. მოხარშე წყალში.\",\"1. Нарезать картофель. 2. Варить в воде.\",\"1. Cut potatoes. 2. Boil in water.\"\n");
    csvTemplate.append(
        "ქართული ხარჩო,Грузинский харчо,Georgian Kharcho,წვნიანები,Супы,Soups,250,Beef,100,Rice,30,Onion,25,Garlic,5,,,45,MEDIUM,\"1. მოხარშე ხორცი. 2. დაამატე ბრინჯი.\",\"1. Отварить мясо. 2. Добавить рис.\",\"1. Boil meat. 2. Add rice.\"\n");
    csvTemplate.append("# \n");
    csvTemplate.append("# Available Products (use exact names):\n");

    products.stream()
        .filter(p -> p.getProductName() != null && !p.getProductName().trim().isEmpty())
        .sorted(
            (p1, p2) -> {
              String cat1 = p1.getCategory() != null ? p1.getCategory() : "Other";
              String cat2 = p2.getCategory() != null ? p2.getCategory() : "Other";
              int catCompare = cat1.compareTo(cat2);
              if (catCompare != 0) return catCompare;
              return p1.getProductName().compareTo(p2.getProductName());
            })
        .forEach(
            product -> {
              String category = product.getCategory() != null ? product.getCategory() : "Other";
              csvTemplate
                  .append("# ")
                  .append(category)
                  .append(": ")
                  .append(product.getProductName())
                  .append("\n");
            });

    csvTemplate.append("# \n");
    csvTemplate.append("# Multi-Language Instructions:\n");
    csvTemplate.append(
        "# - dish_name_XX: Recipe name in each language (ka=Georgian, ru=Russian, en=English)\n");
    csvTemplate.append("# - category_XX: Category name in each language\n");
    csvTemplate.append("# - Use exact product names from the list above\n");
    csvTemplate.append("# - final_dish_weight: Total weight of finished dish in grams\n");
    csvTemplate.append("# - ingredient_X_grams: Weight of each ingredient in grams\n");
    csvTemplate.append("# - difficulty_level: EASY, MEDIUM, or HARD\n");
    csvTemplate.append("# - recipe_instructions_XX: Cooking instructions in each language\n");
    csvTemplate.append("# - Use double quotes for instructions with commas\n");
    csvTemplate.append("# - Leave unused ingredient columns empty\n");
    csvTemplate.append("# \n");
    csvTemplate.append("# Template for your recipes:\n");
    csvTemplate.append(
        "# Your Recipe KA,Your Recipe RU,Your Recipe EN,Category KA,Category RU,Category EN,Weight,Ingredient1,Grams1,Ingredient2,Grams2,,,,,,,,,Time,Difficulty,\"Instructions KA\",\"Instructions RU\",\"Instructions EN\"\n");

    return csvTemplate.toString();
  }
}
