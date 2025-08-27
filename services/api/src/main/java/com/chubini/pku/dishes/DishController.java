package com.chubini.pku.dishes;

import com.chubini.pku.dishes.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dishes")
@Tag(name = "Dishes", description = "CRUD operations for PKU diet dishes (meals composed of multiple products)")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @PostMapping
    @Operation(summary = "Create new dish", description = "Create a new dish with multiple product items and calculate nutritional values")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created dish"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "One or more products not found")
    })
    public ResponseEntity<DishResponseDto> createDish(@Valid @RequestBody DishCreateDto createDto) {
        try {
            DishResponseDto createdDish = dishService.createDish(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDish);
        } catch (DishCalculationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get dish by ID", description = "Retrieve a specific dish with all nutritional details and items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dish"),
        @ApiResponse(responseCode = "404", description = "Dish not found")
    })
    public ResponseEntity<DishResponseDto> getDishById(
            @Parameter(description = "Dish UUID") @PathVariable UUID id) {
        try {
            DishResponseDto dish = dishService.getDishById(id);
            return ResponseEntity.ok(dish);
        } catch (DishNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "List dishes", description = "Get paginated list of dishes with optional search and category filter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dishes"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public Page<DishResponseDto> getAllDishes(
            @Parameter(description = "Search query for dish names") @RequestParam(defaultValue = "") String query,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return dishService.getAllDishes(query, category, page, size);
    }

    @PostMapping("/{id}/scale")
    @Operation(summary = "Scale dish to target grams", description = "Calculate nutritional values when scaling dish to a different serving size")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully calculated scaled values"),
        @ApiResponse(responseCode = "400", description = "Invalid target grams"),
        @ApiResponse(responseCode = "404", description = "Dish not found")
    })
    public ResponseEntity<DishScaleResponseDto> scaleDish(
            @Parameter(description = "Dish UUID") @PathVariable UUID id,
            @Valid @RequestBody DishScaleRequestDto scaleRequest) {
        try {
            DishScaleResponseDto scaledDish = dishService.scaleDish(id, scaleRequest);
            return ResponseEntity.ok(scaledDish);
        } catch (DishNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (DishCalculationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/solve-mass")
    @Operation(summary = "Calculate required dish mass for target nutrients", 
               description = "Calculate how many grams of dish are needed to achieve target phenylalanine (and optionally protein)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully calculated required mass"),
        @ApiResponse(responseCode = "400", description = "Invalid targets or calculation not possible"),
        @ApiResponse(responseCode = "404", description = "Dish not found")
    })
    public ResponseEntity<DishSolveMassResponseDto> solveMass(
            @Parameter(description = "Dish UUID") @PathVariable UUID id,
            @Valid @RequestBody DishSolveMassRequestDto solveRequest) {
        try {
            DishSolveMassResponseDto result = dishService.solveMass(id, solveRequest);
            return ResponseEntity.ok(result);
        } catch (DishNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (DishCalculationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/items")
    @Operation(summary = "Update dish items", description = "Update the composition of a dish by modifying, adding, or removing items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated dish items"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Dish or product not found")
    })
    public ResponseEntity<DishResponseDto> updateDishItems(
            @Parameter(description = "Dish UUID") @PathVariable UUID id,
            @Valid @RequestBody DishUpdateItemsDto updateDto) {
        try {
            DishResponseDto updatedDish = dishService.updateDishItems(id, updateDto);
            return ResponseEntity.ok(updatedDish);
        } catch (DishNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (DishCalculationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete dish", description = "Delete a dish and all its items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted dish"),
        @ApiResponse(responseCode = "404", description = "Dish not found")
    })
    public ResponseEntity<Void> deleteDish(
            @Parameter(description = "Dish UUID") @PathVariable UUID id) {
        try {
            dishService.deleteDish(id);
            return ResponseEntity.noContent().build();
        } catch (DishNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all dish categories", description = "Retrieve list of all dish categories")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = dishService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/low-phe")
    @Operation(summary = "Get low PHE dishes", description = "Get dishes with PHE content below specified maximum per 100g")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dishes"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public Page<DishResponseDto> getLowPheDishes(
            @Parameter(description = "Maximum PHE per 100g") @RequestParam Double maxPhe,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return dishService.getLowPheDishes(maxPhe, page, size);
    }

    // Exception handlers for this controller
    @ExceptionHandler(DishNotFoundException.class)
    public ResponseEntity<String> handleDishNotFound(DishNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(DishCalculationException.class)
    public ResponseEntity<String> handleDishCalculationError(DishCalculationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
