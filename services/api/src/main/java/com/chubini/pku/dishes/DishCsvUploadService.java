package com.chubini.pku.dishes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
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

@Service
@RequiredArgsConstructor
public class DishCsvUploadService {

  private final ProductRepository productRepository;

  public List<Dish> parseDishCsvFile(MultipartFile file) throws IOException {
    return parseDishCsvBytes(file.getBytes());
  }

  public List<Dish> parseDishCsvBytes(byte[] csvData) throws IOException {
    List<Dish> dishes = new ArrayList<>();

    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData);
        InputStreamReader reader = new InputStreamReader(inputStream);
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

      System.out.println("CSV Headers: " + csvParser.getHeaderMap().keySet());

      for (CSVRecord record : csvParser) {
        try {
          System.out.println("Processing record " + record.getRecordNumber() + ": " + record);
          Dish dish = parseDishFromRecord(record);
          if (dish != null) {
            dishes.add(dish);
            System.out.println("Successfully parsed dish: " + dish.getName());
          } else {
            System.err.println("Failed to parse dish from record " + record.getRecordNumber());
          }
        } catch (Exception e) {
          // Log error and continue with next record
          System.err.println(
              "Error parsing dish record "
                  + record.getRecordNumber()
                  + ": "
                  + record
                  + " - "
                  + e.getMessage());
          e.printStackTrace();
        }
      }
    }

    return dishes;
  }

  private Dish parseDishFromRecord(CSVRecord record) {
    try {
      // Parse basic dish information
      String dishName = record.get("dish_name");
      String category = record.get("category");
      String finalDishWeightStr = record.get("final_dish_weight");

      if (dishName == null || dishName.trim().isEmpty()) {
        System.err.println("Missing dish_name at line " + record.getRecordNumber());
        return null;
      }

      // Parse final dish weight (required for per100g calculations)
      BigDecimal finalDishWeight = parseBigDecimal(finalDishWeightStr);
      if (finalDishWeight.compareTo(BigDecimal.ZERO) <= 0) {
        System.err.println(
            "Missing or invalid final_dish_weight at line " + record.getRecordNumber());
        return null;
      }

      // Create dish with basic info - nutrition will be calculated from ingredients
      Dish dish =
          Dish.builder()
              .name(dishName.trim())
              .category(category != null ? category.trim() : "Готовые блюда")
              .nominalServingGrams(finalDishWeight)
              .manualServingOverride(true) // We manually specify the final weight
              .isVerified(false)
              .isVisible(true)
              .build();

      // Parse ingredients (up to 10 ingredients supported)
      List<DishIngredient> ingredients = new ArrayList<>();
      for (int i = 1; i <= 10; i++) {
        String ingredientName = record.get("ingredient_" + i);
        String ingredientGramsStr = record.get("ingredient_" + i + "_grams");

        if (ingredientName != null
            && !ingredientName.trim().isEmpty()
            && ingredientGramsStr != null
            && !ingredientGramsStr.trim().isEmpty()) {

          BigDecimal ingredientGrams = parseBigDecimal(ingredientGramsStr);
          if (ingredientGrams.compareTo(BigDecimal.ZERO) > 0) {

            // Find product by name
            Optional<Product> productOpt = findProductByName(ingredientName.trim());
            if (productOpt.isPresent()) {
              DishIngredient ingredient =
                  DishIngredient.builder()
                      .dish(dish)
                      .product(productOpt.get())
                      .quantityGrams(ingredientGrams)
                      .sortOrder(i)
                      .isOptional(false)
                      .build();
              ingredients.add(ingredient);
            } else {
              System.err.println(
                  "Product not found for ingredient: "
                      + ingredientName
                      + " at line "
                      + record.getRecordNumber());
            }
          }
        }
      }

      dish.setIngredients(ingredients);
      return dish;

    } catch (Exception e) {
      System.err.println("Error parsing dish record: " + e.getMessage());
      return null;
    }
  }

  private BigDecimal parseBigDecimal(String value) {
    if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value)) {
      return BigDecimal.ZERO;
    }
    try {
      // Handle comma as decimal separator
      String cleanValue = value.trim().replace(",", ".");
      return new BigDecimal(cleanValue);
    } catch (NumberFormatException e) {
      System.err.println("Error parsing number: " + value);
      return BigDecimal.ZERO;
    }
  }

  /** Find product by name (case-insensitive, partial match) */
  private Optional<Product> findProductByName(String name) {
    List<Product> products = productRepository.findAll();

    // Try exact match first
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
}
