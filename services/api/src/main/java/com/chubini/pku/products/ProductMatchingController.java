package com.chubini.pku.products;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Controller for intelligent product matching functionality */
@RestController
@RequestMapping("/api/v1/products/matching")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Matching", description = "Intelligent product matching and search APIs")
public class ProductMatchingController {

  private final IntelligentProductMatcher intelligentMatcher;

  @GetMapping("/search")
  @Operation(
      summary = "Intelligent product search",
      description = "Search for products using intelligent fuzzy matching algorithms")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully found matching products"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
      })
  public ResponseEntity<List<IntelligentProductMatcher.ProductMatchResult>> searchProducts(
      @Parameter(description = "Product name or description to search for", required = true)
          @RequestParam
          String query,
      @Parameter(description = "Maximum number of results to return")
          @RequestParam(defaultValue = "10")
          int maxResults) {

    log.info("Intelligent product search for: '{}' (max: {})", query, maxResults);

    if (query == null || query.trim().isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    List<IntelligentProductMatcher.ProductMatchResult> results =
        intelligentMatcher.findProductsIntelligently(query.trim(), maxResults);

    log.info("Found {} matching products for query: '{}'", results.size(), query);
    return ResponseEntity.ok(results);
  }

  @GetMapping("/best-match")
  @Operation(
      summary = "Find best product match",
      description = "Find the single best matching product for a given query")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully found best match"),
        @ApiResponse(responseCode = "404", description = "No suitable match found")
      })
  public ResponseEntity<IntelligentProductMatcher.ProductMatchResult> findBestMatch(
      @Parameter(description = "Product name or description to search for", required = true)
          @RequestParam
          String query) {

    log.info("Finding best product match for: '{}'", query);

    if (query == null || query.trim().isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    return intelligentMatcher
        .findBestMatch(query.trim())
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/suggestions")
  @Operation(
      summary = "Get product suggestions",
      description = "Get intelligent product suggestions based on partial input")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully generated suggestions"),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters")
      })
  public ResponseEntity<List<IntelligentProductMatcher.ProductMatchResult>> getSuggestions(
      @Parameter(description = "Partial product name or description", required = true) @RequestParam
          String partialQuery,
      @Parameter(description = "Minimum confidence threshold (0.0-1.0)")
          @RequestParam(defaultValue = "0.3")
          double minConfidence,
      @Parameter(description = "Maximum number of suggestions") @RequestParam(defaultValue = "5")
          int maxSuggestions) {

    log.info(
        "Getting product suggestions for: '{}' (min confidence: {}, max: {})",
        partialQuery,
        minConfidence,
        maxSuggestions);

    if (partialQuery == null || partialQuery.trim().isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    List<IntelligentProductMatcher.ProductMatchResult> results =
        intelligentMatcher.findProductsIntelligently(partialQuery.trim(), maxSuggestions * 2);

    // Filter by minimum confidence
    List<IntelligentProductMatcher.ProductMatchResult> filteredResults =
        results.stream()
            .filter(result -> result.getConfidence() >= minConfidence)
            .limit(maxSuggestions)
            .toList();

    log.info(
        "Generated {} suggestions for partial query: '{}'", filteredResults.size(), partialQuery);
    return ResponseEntity.ok(filteredResults);
  }
}
