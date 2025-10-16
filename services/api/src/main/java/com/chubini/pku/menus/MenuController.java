package com.chubini.pku.menus;

import java.util.List;
import java.util.UUID;

import com.chubini.pku.generator.SnackSuggestionService;
import com.chubini.pku.generator.dto.SnackSuggestion;
import com.chubini.pku.menus.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Menu Management", description = "APIs for managing daily and weekly menus")
public class MenuController {

  private final MenuService menuService;
  private final SnackSuggestionService snackSuggestionService;

  // ========== Weekly Menu Endpoints ==========

  @Operation(
      summary = "Get menu weeks for a patient",
      description = "Retrieve all menu weeks for a specific patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved menu weeks",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MenuWeekDto.class)))
      })
  @GetMapping("/weeks/patient/{patientId}")
  public ResponseEntity<List<MenuWeekDto>> getMenuWeeksByPatient(
      @Parameter(description = "Patient unique identifier", required = true) @PathVariable
          UUID patientId) {

    log.info("Getting menu weeks for patient: {}", patientId);
    List<MenuWeekDto> weeks = menuService.getMenuWeeksByPatient(patientId);
    return ResponseEntity.ok(weeks);
  }

  @Operation(
      summary = "Get menu week by ID",
      description = "Retrieve a specific menu week by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved menu week",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MenuWeekDto.class))),
        @ApiResponse(responseCode = "404", description = "Menu week not found")
      })
  @GetMapping("/weeks/{weekId}")
  public ResponseEntity<MenuWeekDto> getMenuWeekById(
      @Parameter(description = "Menu week unique identifier", required = true) @PathVariable
          UUID weekId) {

    log.info("Getting menu week by ID: {}", weekId);
    MenuWeekDto week = menuService.getMenuWeekById(weekId);
    return ResponseEntity.ok(week);
  }

  @Operation(
      summary = "Create new menu week",
      description = "Create a new weekly menu for a patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Menu week created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MenuWeekDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
      })
  @PostMapping("/weeks")
  public ResponseEntity<MenuWeekDto> createMenuWeek(
      @Parameter(description = "Menu week creation request", required = true) @Valid @RequestBody
          CreateMenuWeekRequest request) {

    log.info("Creating new menu week for patient: {}", request.patientId());
    MenuWeekDto createdWeek = menuService.createMenuWeek(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdWeek);
  }

  @Operation(
      summary = "Delete menu week",
      description = "Delete a menu week and all associated daily menus")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Menu week deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Menu week not found")
      })
  @DeleteMapping("/weeks/{weekId}")
  public ResponseEntity<Void> deleteMenuWeek(
      @Parameter(description = "Menu week unique identifier", required = true) @PathVariable
          UUID weekId) {

    log.info("Deleting menu week: {}", weekId);
    menuService.deleteMenuWeek(weekId);
    return ResponseEntity.noContent().build();
  }

  // ========== Daily Menu Endpoints ==========

  @Operation(
      summary = "Get menu days for a week",
      description = "Retrieve all daily menus for a specific week")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved menu days",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MenuDayDto.class)))
      })
  @GetMapping("/weeks/{weekId}/days")
  public ResponseEntity<List<MenuDayDto>> getMenuDaysByWeek(
      @Parameter(description = "Menu week unique identifier", required = true) @PathVariable
          UUID weekId) {

    log.info("Getting menu days for week: {}", weekId);
    List<MenuDayDto> days = menuService.getMenuDaysByWeek(weekId);
    return ResponseEntity.ok(days);
  }

  @Operation(
      summary = "Get menu day by ID",
      description = "Retrieve a specific daily menu by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved menu day",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MenuDayDto.class))),
        @ApiResponse(responseCode = "404", description = "Menu day not found")
      })
  @GetMapping("/days/{dayId}")
  public ResponseEntity<MenuDayDto> getMenuDayById(
      @Parameter(description = "Menu day unique identifier", required = true) @PathVariable
          UUID dayId) {

    log.info("Getting menu day by ID: {}", dayId);
    MenuDayDto day = menuService.getMenuDayById(dayId);
    return ResponseEntity.ok(day);
  }

  @Operation(summary = "Create new menu day", description = "Create a new daily menu")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Menu day created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MenuDayDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
      })
  @PostMapping("/days")
  public ResponseEntity<MenuDayDto> createMenuDay(
      @Parameter(description = "Menu day creation request", required = true) @Valid @RequestBody
          CreateMenuDayRequest request) {

    log.info("Creating new menu day for date: {}", request.menuDate());
    MenuDayDto createdDay = menuService.createMenuDay(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdDay);
  }

  @Operation(
      summary = "Delete menu day",
      description = "Delete a daily menu and all associated meal slots")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Menu day deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Menu day not found")
      })
  @DeleteMapping("/days/{dayId}")
  public ResponseEntity<Void> deleteMenuDay(
      @Parameter(description = "Menu day unique identifier", required = true) @PathVariable
          UUID dayId) {

    log.info("Deleting menu day: {}", dayId);
    menuService.deleteMenuDay(dayId);
    return ResponseEntity.noContent().build();
  }

  // ========== Meal Slot Endpoints ==========

  @Operation(
      summary = "Get meal slots for a day",
      description = "Retrieve all meal slots for a specific daily menu")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved meal slots",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MealSlotDto.class)))
      })
  @GetMapping("/days/{dayId}/slots")
  public ResponseEntity<List<MealSlotDto>> getMealSlotsByDay(
      @Parameter(description = "Menu day unique identifier", required = true) @PathVariable
          UUID dayId) {

    log.info("Getting meal slots for day: {}", dayId);
    List<MealSlotDto> slots = menuService.getMealSlotsByDay(dayId);
    return ResponseEntity.ok(slots);
  }

  // ========== Menu Entry Endpoints ==========

  @Operation(
      summary = "Get menu entries for a slot",
      description = "Retrieve all menu entries for a specific meal slot")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved menu entries",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MenuEntryDto.class)))
      })
  @GetMapping("/slots/{slotId}/entries")
  public ResponseEntity<List<MenuEntryDto>> getMenuEntriesBySlot(
      @Parameter(description = "Meal slot unique identifier", required = true) @PathVariable
          UUID slotId) {

    log.info("Getting menu entries for slot: {}", slotId);
    List<MenuEntryDto> entries = menuService.getMenuEntriesBySlot(slotId);
    return ResponseEntity.ok(entries);
  }

  @Operation(summary = "Add menu entry to slot", description = "Add a new food item to a meal slot")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Menu entry added successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MenuEntryDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Meal slot not found")
      })
  @PostMapping("/slots/{slotId}/entries")
  public ResponseEntity<MenuEntryDto> addMenuEntry(
      @Parameter(description = "Meal slot unique identifier", required = true) @PathVariable
          UUID slotId,
      @Parameter(description = "Menu entry creation request", required = true) @Valid @RequestBody
          AddMenuEntryRequest request) {

    log.info("Adding menu entry to slot: {}", slotId);
    MenuEntryDto createdEntry = menuService.addMenuEntry(slotId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdEntry);
  }

  @Operation(summary = "Update menu entry", description = "Update an existing menu entry")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Menu entry updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MenuEntryDto.class))),
        @ApiResponse(responseCode = "404", description = "Menu entry not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
      })
  @PutMapping("/entries/{entryId}")
  public ResponseEntity<MenuEntryDto> updateMenuEntry(
      @Parameter(description = "Menu entry unique identifier", required = true) @PathVariable
          UUID entryId,
      @Parameter(description = "Menu entry update request", required = true) @Valid @RequestBody
          UpdateMenuEntryRequest request) {

    log.info("Updating menu entry: {}", entryId);
    MenuEntryDto updatedEntry = menuService.updateMenuEntry(entryId, request);
    return ResponseEntity.ok(updatedEntry);
  }

  @Operation(summary = "Delete menu entry", description = "Remove a menu entry from a meal slot")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Menu entry deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Menu entry not found")
      })
  @DeleteMapping("/entries/{entryId}")
  public ResponseEntity<Void> deleteMenuEntry(
      @Parameter(description = "Menu entry unique identifier", required = true) @PathVariable
          UUID entryId) {

    log.info("Deleting menu entry: {}", entryId);
    menuService.deleteMenuEntry(entryId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Update consumed quantity",
      description = "Update the consumed quantity for a menu entry and recalculate day totals")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Menu entry consumed quantity updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(
                            implementation =
                                com.chubini.pku.validation.dto.DayValidationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Menu entry not found"),
        @ApiResponse(responseCode = "400", description = "Invalid consumed quantity")
      })
  @PatchMapping("/entries/{entryId}/consumed")
  public ResponseEntity<com.chubini.pku.validation.dto.DayValidationResponse>
      updateConsumedQuantity(
          @Parameter(description = "Menu entry unique identifier", required = true) @PathVariable
              UUID entryId,
          @Parameter(description = "Consumed quantity update request", required = true)
              @Valid
              @RequestBody
              UpdateConsumedQuantityRequest request) {

    log.info("Updating consumed quantity for menu entry {}: {}", entryId, request.consumedQty());
    com.chubini.pku.validation.dto.DayValidationResponse response =
        menuService.updateConsumedQuantity(entryId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Mark entry as consumed (legacy)",
      description =
          "Mark a menu entry as consumed or not consumed - use updateConsumedQuantity for better control")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Menu entry consumption status updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MenuEntryDto.class))),
        @ApiResponse(responseCode = "404", description = "Menu entry not found")
      })
  @PatchMapping("/entries/{entryId}/consumed/status")
  public ResponseEntity<MenuEntryDto> markEntryAsConsumed(
      @Parameter(description = "Menu entry unique identifier", required = true) @PathVariable
          UUID entryId,
      @Parameter(description = "Consumed status", required = true) @RequestParam boolean consumed) {

    log.info("Marking menu entry {} as consumed: {}", entryId, consumed);
    MenuEntryDto updatedEntry = menuService.markEntryAsConsumed(entryId, consumed);
    return ResponseEntity.ok(updatedEntry);
  }

  // ========== Snack Suggestions Endpoint ==========

  @Operation(
      summary = "Get snack suggestions",
      description =
          "Get snack suggestions when calorie deficit exists and core meals cannot be safely increased")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Snack suggestions retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(implementation = SnackSuggestion.SnackSuggestionsResponse.class))),
        @ApiResponse(responseCode = "404", description = "Menu day not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
      })
  @GetMapping("/days/{id}/suggestions")
  public ResponseEntity<SnackSuggestion.SnackSuggestionsResponse> getSnackSuggestions(
      @Parameter(description = "Menu day unique identifier", required = true) @PathVariable UUID id,
      @Parameter(description = "Type of suggestions to return", example = "snack")
          @RequestParam(defaultValue = "snack")
          String type) {

    log.info("Getting snack suggestions for menu day: {}", id);

    if (!"snack".equals(type)) {
      log.warn("Unsupported suggestion type: {}", type);
      return ResponseEntity.badRequest().build();
    }

    SnackSuggestion.SnackSuggestionsResponse response =
        snackSuggestionService.generateSnackSuggestions(id);
    return ResponseEntity.ok(response);
  }
}
