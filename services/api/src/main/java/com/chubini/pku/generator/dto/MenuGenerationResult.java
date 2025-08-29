package com.chubini.pku.generator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Result of menu generation operation")
public record MenuGenerationResult(
    @Schema(description = "Whether generation was successful", example = "true")
    boolean success,

    @Schema(description = "Generated menu ID (week or day)", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID menuId,

    @Schema(description = "Result message", example = "Menu generated successfully")
    String message,

    @Schema(description = "Generation timestamp")
    LocalDateTime generatedAt,

    @Schema(description = "Any warnings during generation")
    List<String> warnings,

    @Schema(description = "Error details if generation failed")
    String errorDetails
) {
    
    public static MenuGenerationResult success(UUID menuId, String message) {
        return new MenuGenerationResult(
                true,
                menuId,
                message,
                LocalDateTime.now(),
                List.of(),
                null
        );
    }
    
    public static MenuGenerationResult success(UUID menuId, String message, List<String> warnings) {
        return new MenuGenerationResult(
                true,
                menuId,
                message,
                LocalDateTime.now(),
                warnings,
                null
        );
    }
    
    public static MenuGenerationResult failure(String errorMessage) {
        return new MenuGenerationResult(
                false,
                null,
                "Generation failed",
                LocalDateTime.now(),
                List.of(),
                errorMessage
        );
    }
    
    public static MenuGenerationResult failure(String message, String errorDetails) {
        return new MenuGenerationResult(
                false,
                null,
                message,
                LocalDateTime.now(),
                List.of(),
                errorDetails
        );
    }
}
