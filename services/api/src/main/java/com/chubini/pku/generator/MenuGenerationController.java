package com.chubini.pku.generator;

import com.chubini.pku.generator.dto.MenuGenerationRequest;
import com.chubini.pku.generator.dto.MenuGenerationResult;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/generator")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Menu Generation", description = "APIs for automated menu generation using heuristic algorithms")
public class MenuGenerationController {

    private final MenuGenerationService menuGenerationService;

    @Operation(summary = "Generate weekly menu", description = "Generate a complete weekly menu using heuristic algorithm based on patient's nutritional requirements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Weekly menu generated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MenuGenerationResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid generation request"),
            @ApiResponse(responseCode = "404", description = "Patient not found or no active norm prescription"),
            @ApiResponse(responseCode = "500", description = "Menu generation failed")
    })
    @PostMapping("/weekly")
    public ResponseEntity<MenuGenerationResult> generateWeeklyMenu(
            @Parameter(description = "Weekly menu generation request", required = true)
            @Valid @RequestBody MenuGenerationRequest request) {
        
        log.info("Generating weekly menu for patient: {} starting {}", request.patientId(), request.startDate());
        
        MenuGenerationResult result = menuGenerationService.generateWeeklyMenu(request);
        
        if (result.success()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @Operation(summary = "Generate daily menu", description = "Generate a single daily menu using heuristic algorithm based on patient's nutritional requirements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily menu generated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MenuGenerationResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid generation request"),
            @ApiResponse(responseCode = "404", description = "Patient not found or no active norm prescription"),
            @ApiResponse(responseCode = "500", description = "Menu generation failed")
    })
    @PostMapping("/daily")
    public ResponseEntity<MenuGenerationResult> generateDailyMenu(
            @Parameter(description = "Daily menu generation request", required = true)
            @Valid @RequestBody MenuGenerationRequest request) {
        
        log.info("Generating daily menu for patient: {} on {}", request.patientId(), request.startDate());
        
        MenuGenerationResult result = menuGenerationService.generateDailyMenu(request);
        
        if (result.success()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
