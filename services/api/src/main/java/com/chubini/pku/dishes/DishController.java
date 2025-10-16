package com.chubini.pku.dishes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dishes")
@Tag(name = "Dishes", description = "CRUD operations for PKU diet dishes")
@RequiredArgsConstructor
public class DishController {

  private final DishService dishService;
  private final DishCsvUploadService dishCsvUploadService;
  private final MultiLanguageDishCsvService multiLanguageDishCsvService;

  @GetMapping
  @Operation(summary = "Get all dishes", description = "Get paginated list of all visible dishes")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dishes"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
      })
  public Page<Dish> getAllDishes(
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
    return dishService.getAllDishes(page, size);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get dish by ID", description = "Get a specific dish by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dish"),
        @ApiResponse(responseCode = "404", description = "Dish not found")
      })
  public ResponseEntity<Dish> getDishById(
      @Parameter(description = "Dish ID") @PathVariable UUID id) {
    return dishService
        .getDishById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/category/{category}")
  @Operation(
      summary = "Get dishes by category",
      description = "Get paginated list of dishes in a specific category")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dishes"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
      })
  public Page<Dish> getDishesByCategory(
      @Parameter(description = "Dish category") @PathVariable String category,
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
    return dishService.getDishesByCategory(category, page, size);
  }

  @GetMapping("/categories")
  @Operation(
      summary = "Get all dish categories",
      description = "Get list of all distinct dish categories")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved categories")
  public List<String> getAllCategories() {
    return dishService.getAllCategories();
  }

  @GetMapping("/search")
  @Operation(summary = "Search dishes", description = "Search dishes by name")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dishes"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
      })
  public Page<Dish> searchDishes(
      @Parameter(description = "Search term") @RequestParam String q,
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
    return dishService.searchDishes(q, page, size);
  }

  @GetMapping("/low-phe")
  @Operation(
      summary = "Get low PHE dishes",
      description = "Get dishes with PHE content below specified maximum")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dishes"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
      })
  public Page<Dish> getLowPheDishes(
      @Parameter(description = "Maximum PHE per 100g") @RequestParam Double maxPhe,
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
    return dishService.getLowPheDishes(maxPhe, page, size);
  }

  @PostMapping
  @Operation(
      summary = "Create new dish",
      description = "Create a new dish with ingredients and nutritional calculations")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Successfully created dish"),
        @ApiResponse(responseCode = "400", description = "Invalid dish data")
      })
  public ResponseEntity<Dish> createDish(@Valid @RequestBody Dish dish) {
    Dish createdDish = dishService.createDish(dish);
    return ResponseEntity.ok(createdDish);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update dish", description = "Update an existing dish")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated dish"),
        @ApiResponse(responseCode = "404", description = "Dish not found"),
        @ApiResponse(responseCode = "400", description = "Invalid dish data")
      })
  public ResponseEntity<Dish> updateDish(
      @Parameter(description = "Dish ID") @PathVariable UUID id,
      @Valid @RequestBody Dish dishDetails) {
    try {
      Dish updatedDish = dishService.updateDish(id, dishDetails);
      return ResponseEntity.ok(updatedDish);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete dish", description = "Soft delete a dish (mark as invisible)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted dish"),
        @ApiResponse(responseCode = "404", description = "Dish not found")
      })
  public ResponseEntity<Void> deleteDish(
      @Parameter(description = "Dish ID") @PathVariable UUID id) {
    try {
      dishService.deleteDish(id);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/csv-template")
  @Operation(
      summary = "Generate CSV template for dish upload",
      description = "Generate a CSV template with all available products for creating dish recipes")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully generated CSV template"),
        @ApiResponse(responseCode = "500", description = "Error generating template")
      })
  public ResponseEntity<String> generateCsvTemplate() {
    try {
      String csvTemplate = dishService.generateCsvTemplate();
      return ResponseEntity.ok()
          .header("Content-Type", "text/csv; charset=UTF-8")
          .header("Content-Disposition", "attachment; filename=\"dish_template.csv\"")
          .body(csvTemplate);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("Error generating CSV template: " + e.getMessage());
    }
  }

  @PostMapping("/upload-csv")
  @Operation(
      summary = "Upload dishes from CSV",
      description = "Upload multiple dishes from a CSV file")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully uploaded dishes"),
        @ApiResponse(responseCode = "400", description = "Invalid CSV file or data")
      })
  public ResponseEntity<String> uploadDishesFromCsv(
      @Parameter(description = "CSV file containing dish data") @RequestParam("file")
          MultipartFile file) {
    try {
      List<Dish> dishes = dishCsvUploadService.parseDishCsvFile(file);

      if (dishes.isEmpty()) {
        return ResponseEntity.badRequest().body("No valid dishes found in CSV file");
      }

      // Save all dishes
      int savedCount = 0;
      StringBuilder errors = new StringBuilder();

      for (Dish dish : dishes) {
        try {
          dishService.createDish(dish);
          savedCount++;
        } catch (Exception e) {
          errors
              .append("Error saving dish '")
              .append(dish.getName())
              .append("': ")
              .append(e.getMessage())
              .append("; ");
        }
      }

      String result =
          String.format("Successfully uploaded %d dishes out of %d", savedCount, dishes.size());
      if (errors.length() > 0) {
        result += ". Errors: " + errors.toString();
      }

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error uploading CSV: " + e.getMessage());
    }
  }

  @PostMapping("/upload-multilang-csv")
  @Operation(
      summary = "Upload multi-language dishes from CSV",
      description =
          "Upload multiple dishes with Georgian, Russian, and English translations from a CSV file")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully uploaded multi-language dishes"),
        @ApiResponse(responseCode = "400", description = "Invalid CSV file or data")
      })
  public ResponseEntity<String> uploadMultiLanguageDishesFromCsv(
      @Parameter(description = "CSV file containing multi-language dish data") @RequestParam("file")
          MultipartFile file) {
    try {
      List<MultiLanguageDishCsvService.MultiLanguageDishDto> dishDtos =
          multiLanguageDishCsvService.parseMultiLanguageCsvFile(file);

      if (dishDtos.isEmpty()) {
        return ResponseEntity.badRequest().body("No valid dishes found in CSV file");
      }

      // Save all dishes with translations
      int savedCount = 0;
      StringBuilder errors = new StringBuilder();

      for (MultiLanguageDishCsvService.MultiLanguageDishDto dishDto : dishDtos) {
        try {
          // Create dish for each language
          createDishWithTranslations(dishDto);
          savedCount++;
        } catch (Exception e) {
          errors
              .append("Error saving dish '")
              .append(dishDto.getNameEn())
              .append("': ")
              .append(e.getMessage())
              .append("; ");
        }
      }

      String result =
          String.format(
              "Successfully uploaded %d multi-language dishes out of %d",
              savedCount, dishDtos.size());
      if (errors.length() > 0) {
        result += ". Errors: " + errors.toString();
      }

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body("Error uploading multi-language CSV: " + e.getMessage());
    }
  }

  @GetMapping("/multilang-csv-template")
  @Operation(
      summary = "Generate multi-language CSV template",
      description =
          "Generate a CSV template for multi-language dish upload with Georgian, Russian, and English support")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully generated multi-language CSV template"),
        @ApiResponse(responseCode = "500", description = "Error generating template")
      })
  public ResponseEntity<String> generateMultiLanguageCsvTemplate() {
    try {
      String csvTemplate = dishService.generateMultiLanguageCsvTemplate();
      return ResponseEntity.ok()
          .header("Content-Type", "text/csv; charset=UTF-8")
          .header("Content-Disposition", "attachment; filename=\"multilang_dish_template.csv\"")
          .body(csvTemplate);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("Error generating multi-language CSV template: " + e.getMessage());
    }
  }

  private void createDishWithTranslations(
      MultiLanguageDishCsvService.MultiLanguageDishDto dishDto) {
    // Create base dish (English version)
    Dish dish =
        Dish.builder()
            .name(dishDto.getNameEn())
            .category(dishDto.getCategoryEn())
            .nominalServingGrams(dishDto.getFinalDishWeight())
            .preparationTimeMinutes(dishDto.getPreparationTimeMinutes())
            .difficultyLevel(dishDto.getDifficultyLevel())
            .recipeInstructions(dishDto.getInstructionsEn())
            .isVerified(false)
            .isVisible(true)
            .build();

    // Add ingredients
    List<DishIngredient> ingredients = new ArrayList<>();
    for (MultiLanguageDishCsvService.DishIngredientDto ingredientDto : dishDto.getIngredients()) {
      DishIngredient ingredient =
          DishIngredient.builder()
              .dish(dish)
              .product(ingredientDto.getProduct())
              .quantityGrams(ingredientDto.getQuantityGrams())
              .build();
      ingredients.add(ingredient);
    }
    dish.setIngredients(ingredients);

    // Save the dish
    dishService.createDish(dish);

    // TODO: Add translation support for Georgian and Russian versions
    // This would require a translation table/service to store dish names and instructions
    // in multiple languages, similar to how products have translations
  }
}
