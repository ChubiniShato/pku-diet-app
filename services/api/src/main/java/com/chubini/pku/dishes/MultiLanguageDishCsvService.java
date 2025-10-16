package com.chubini.pku.dishes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.chubini.pku.products.Product;
import com.chubini.pku.products.ProductRepository;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiLanguageDishCsvService {

  private final ProductRepository productRepository;
  private final DishService dishService;

  public List<MultiLanguageDishDto> parseMultiLanguageCsvFile(MultipartFile file)
      throws IOException {
    return parseMultiLanguageCsvBytes(file.getBytes());
  }

  public List<MultiLanguageDishDto> parseMultiLanguageCsvBytes(byte[] csvData) throws IOException {
    List<MultiLanguageDishDto> dishes = new ArrayList<>();

    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData);
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        CSVParser csvParser =
            new CSVParser(
                reader,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

      for (CSVRecord record : csvParser) {
        // Skip comment lines
        if (record.get(0).startsWith("#")) {
          continue;
        }

        try {
          MultiLanguageDishDto dish = parseMultiLanguageDishRecord(record);
          if (dish != null) {
            dishes.add(dish);
          }
        } catch (Exception e) {
          log.error("Error parsing multi-language dish record: {}", e.getMessage());
          throw new RuntimeException(
              "Failed to parse dish from record " + record.getRecordNumber(), e);
        }
      }
    }

    return dishes;
  }

  private MultiLanguageDishDto parseMultiLanguageDishRecord(CSVRecord record) {
    try {
      // Extract multi-language names
      String nameKa = getFieldValue(record, "dish_name_ka");
      String nameRu = getFieldValue(record, "dish_name_ru");
      String nameEn = getFieldValue(record, "dish_name_en");

      // Extract multi-language categories
      String categoryKa = getFieldValue(record, "category_ka");
      String categoryRu = getFieldValue(record, "category_ru");
      String categoryEn = getFieldValue(record, "category_en");

      // Extract basic dish info
      String finalDishWeightStr = getFieldValue(record, "final_dish_weight");
      String preparationTimeStr = getFieldValue(record, "preparation_time_minutes");
      String difficultyLevel = getFieldValue(record, "difficulty_level");

      // Extract multi-language instructions
      String instructionsKa = getFieldValue(record, "recipe_instructions_ka");
      String instructionsRu = getFieldValue(record, "recipe_instructions_ru");
      String instructionsEn = getFieldValue(record, "recipe_instructions_en");

      // Validate required fields
      if (nameEn == null || nameEn.trim().isEmpty()) {
        log.warn("Skipping record with missing English name at line {}", record.getRecordNumber());
        return null;
      }

      if (finalDishWeightStr == null || finalDishWeightStr.trim().isEmpty()) {
        log.warn(
            "Skipping record with missing final dish weight at line {}", record.getRecordNumber());
        return null;
      }

      // Parse ingredients
      List<DishIngredientDto> ingredients = parseIngredients(record);
      if (ingredients.isEmpty()) {
        log.warn("Skipping record with no valid ingredients at line {}", record.getRecordNumber());
        return null;
      }

      // Build multi-language dish DTO
      return MultiLanguageDishDto.builder()
          .nameKa(nameKa)
          .nameRu(nameRu)
          .nameEn(nameEn)
          .categoryKa(categoryKa)
          .categoryRu(categoryRu)
          .categoryEn(categoryEn)
          .finalDishWeight(new BigDecimal(finalDishWeightStr))
          .ingredients(ingredients)
          .preparationTimeMinutes(
              preparationTimeStr != null && !preparationTimeStr.trim().isEmpty()
                  ? Integer.parseInt(preparationTimeStr)
                  : null)
          .difficultyLevel(
              difficultyLevel != null && !difficultyLevel.trim().isEmpty()
                  ? Dish.DifficultyLevel.valueOf(difficultyLevel.toUpperCase())
                  : Dish.DifficultyLevel.EASY)
          .instructionsKa(instructionsKa)
          .instructionsRu(instructionsRu)
          .instructionsEn(instructionsEn)
          .build();

    } catch (Exception e) {
      log.error(
          "Error parsing multi-language dish record at line {}: {}",
          record.getRecordNumber(),
          e.getMessage());
      throw new RuntimeException("Error parsing dish record: " + e.getMessage(), e);
    }
  }

  private List<DishIngredientDto> parseIngredients(CSVRecord record) {
    List<DishIngredientDto> ingredients = new ArrayList<>();

    // Parse up to 6 ingredients
    for (int i = 1; i <= 6; i++) {
      String ingredientName = getFieldValue(record, "ingredient_" + i);
      String ingredientGramsStr = getFieldValue(record, "ingredient_" + i + "_grams");

      if (ingredientName != null
          && !ingredientName.trim().isEmpty()
          && ingredientGramsStr != null
          && !ingredientGramsStr.trim().isEmpty()) {

        // Find product in database
        Optional<Product> productOpt = dishService.findProductByName(ingredientName.trim());
        if (productOpt.isEmpty()) {
          log.error(
              "Product not found for ingredient: {} at line {}",
              ingredientName,
              record.getRecordNumber());
          throw new RuntimeException("Product not found: " + ingredientName);
        }

        try {
          BigDecimal grams = new BigDecimal(ingredientGramsStr.trim());
          ingredients.add(
              DishIngredientDto.builder().product(productOpt.get()).quantityGrams(grams).build());
        } catch (NumberFormatException e) {
          log.error(
              "Invalid ingredient weight: {} for ingredient: {}",
              ingredientGramsStr,
              ingredientName);
          throw new RuntimeException("Invalid ingredient weight: " + ingredientGramsStr);
        }
      }
    }

    return ingredients;
  }

  private String getFieldValue(CSVRecord record, String fieldName) {
    try {
      if (record.isMapped(fieldName)) {
        String value = record.get(fieldName);
        return value != null && !value.trim().isEmpty() ? value.trim() : null;
      }
    } catch (IllegalArgumentException e) {
      // Field not found in CSV
    }
    return null;
  }

  // DTO classes
  public static class MultiLanguageDishDto {
    private String nameKa;
    private String nameRu;
    private String nameEn;
    private String categoryKa;
    private String categoryRu;
    private String categoryEn;
    private BigDecimal finalDishWeight;
    private List<DishIngredientDto> ingredients;
    private Integer preparationTimeMinutes;
    private Dish.DifficultyLevel difficultyLevel;
    private String instructionsKa;
    private String instructionsRu;
    private String instructionsEn;

    public static MultiLanguageDishDtoBuilder builder() {
      return new MultiLanguageDishDtoBuilder();
    }

    // Getters
    public String getNameKa() {
      return nameKa;
    }

    public String getNameRu() {
      return nameRu;
    }

    public String getNameEn() {
      return nameEn;
    }

    public String getCategoryKa() {
      return categoryKa;
    }

    public String getCategoryRu() {
      return categoryRu;
    }

    public String getCategoryEn() {
      return categoryEn;
    }

    public BigDecimal getFinalDishWeight() {
      return finalDishWeight;
    }

    public List<DishIngredientDto> getIngredients() {
      return ingredients;
    }

    public Integer getPreparationTimeMinutes() {
      return preparationTimeMinutes;
    }

    public Dish.DifficultyLevel getDifficultyLevel() {
      return difficultyLevel;
    }

    public String getInstructionsKa() {
      return instructionsKa;
    }

    public String getInstructionsRu() {
      return instructionsRu;
    }

    public String getInstructionsEn() {
      return instructionsEn;
    }

    public static class MultiLanguageDishDtoBuilder {
      private String nameKa;
      private String nameRu;
      private String nameEn;
      private String categoryKa;
      private String categoryRu;
      private String categoryEn;
      private BigDecimal finalDishWeight;
      private List<DishIngredientDto> ingredients;
      private Integer preparationTimeMinutes;
      private Dish.DifficultyLevel difficultyLevel;
      private String instructionsKa;
      private String instructionsRu;
      private String instructionsEn;

      public MultiLanguageDishDtoBuilder nameKa(String nameKa) {
        this.nameKa = nameKa;
        return this;
      }

      public MultiLanguageDishDtoBuilder nameRu(String nameRu) {
        this.nameRu = nameRu;
        return this;
      }

      public MultiLanguageDishDtoBuilder nameEn(String nameEn) {
        this.nameEn = nameEn;
        return this;
      }

      public MultiLanguageDishDtoBuilder categoryKa(String categoryKa) {
        this.categoryKa = categoryKa;
        return this;
      }

      public MultiLanguageDishDtoBuilder categoryRu(String categoryRu) {
        this.categoryRu = categoryRu;
        return this;
      }

      public MultiLanguageDishDtoBuilder categoryEn(String categoryEn) {
        this.categoryEn = categoryEn;
        return this;
      }

      public MultiLanguageDishDtoBuilder finalDishWeight(BigDecimal finalDishWeight) {
        this.finalDishWeight = finalDishWeight;
        return this;
      }

      public MultiLanguageDishDtoBuilder ingredients(List<DishIngredientDto> ingredients) {
        this.ingredients = ingredients;
        return this;
      }

      public MultiLanguageDishDtoBuilder preparationTimeMinutes(Integer preparationTimeMinutes) {
        this.preparationTimeMinutes = preparationTimeMinutes;
        return this;
      }

      public MultiLanguageDishDtoBuilder difficultyLevel(Dish.DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
        return this;
      }

      public MultiLanguageDishDtoBuilder instructionsKa(String instructionsKa) {
        this.instructionsKa = instructionsKa;
        return this;
      }

      public MultiLanguageDishDtoBuilder instructionsRu(String instructionsRu) {
        this.instructionsRu = instructionsRu;
        return this;
      }

      public MultiLanguageDishDtoBuilder instructionsEn(String instructionsEn) {
        this.instructionsEn = instructionsEn;
        return this;
      }

      public MultiLanguageDishDto build() {
        MultiLanguageDishDto dto = new MultiLanguageDishDto();
        dto.nameKa = this.nameKa;
        dto.nameRu = this.nameRu;
        dto.nameEn = this.nameEn;
        dto.categoryKa = this.categoryKa;
        dto.categoryRu = this.categoryRu;
        dto.categoryEn = this.categoryEn;
        dto.finalDishWeight = this.finalDishWeight;
        dto.ingredients = this.ingredients;
        dto.preparationTimeMinutes = this.preparationTimeMinutes;
        dto.difficultyLevel = this.difficultyLevel;
        dto.instructionsKa = this.instructionsKa;
        dto.instructionsRu = this.instructionsRu;
        dto.instructionsEn = this.instructionsEn;
        return dto;
      }
    }
  }

  public static class DishIngredientDto {
    private Product product;
    private BigDecimal quantityGrams;

    public static DishIngredientDtoBuilder builder() {
      return new DishIngredientDtoBuilder();
    }

    public Product getProduct() {
      return product;
    }

    public BigDecimal getQuantityGrams() {
      return quantityGrams;
    }

    public static class DishIngredientDtoBuilder {
      private Product product;
      private BigDecimal quantityGrams;

      public DishIngredientDtoBuilder product(Product product) {
        this.product = product;
        return this;
      }

      public DishIngredientDtoBuilder quantityGrams(BigDecimal quantityGrams) {
        this.quantityGrams = quantityGrams;
        return this;
      }

      public DishIngredientDto build() {
        DishIngredientDto dto = new DishIngredientDto();
        dto.product = this.product;
        dto.quantityGrams = this.quantityGrams;
        return dto;
      }
    }
  }
}
