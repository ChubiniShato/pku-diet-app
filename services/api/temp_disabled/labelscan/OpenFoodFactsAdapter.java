package com.chubini.pku.labelscan;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/** OpenFoodFacts barcode lookup adapter */
@Service
@Slf4j
public class OpenFoodFactsAdapter implements BarcodeLookupService {

  @Value("${openfoodfacts.enabled:true}")
  private boolean enabled;

  @Value("${openfoodfacts.api.url:https://world.openfoodfacts.org/api/v0/product/}")
  private String apiUrl;

  @Value("${openfoodfacts.timeout:5000}")
  private int timeoutMs;

  private final RestTemplate restTemplate;

  public OpenFoodFactsAdapter() {
    this.restTemplate = new RestTemplate();
    // Configure timeouts if needed
  }

  @Override
  public BarcodeLookupResult lookupByBarcode(String barcode, String region) {
    if (!enabled) {
      log.debug("OpenFoodFacts lookup disabled, returning mock result");
      return createMockResult(barcode);
    }

    if (barcode == null || barcode.trim().isEmpty()) {
      return BarcodeLookupResult.builder().errorMessage("Barcode is empty").build();
    }

    try {
      String url = apiUrl + barcode + ".json";
      log.debug("Looking up barcode {} via OpenFoodFacts: {}", barcode, url);

      OpenFoodFactsResponse response = restTemplate.getForObject(url, OpenFoodFactsResponse.class);

      if (response == null || response.product == null) {
        log.debug("Product not found for barcode: {}", barcode);
        return BarcodeLookupResult.builder()
            .errorMessage("Product not found")
            .metadata(Map.of("source", "OpenFoodFacts", "url", url))
            .build();
      }

      ProductInfo productInfo = extractProductInfo(response.product, barcode);
      double confidence = calculateConfidence(response.product);

      return BarcodeLookupResult.builder()
          .productInfo(productInfo)
          .metadata(
              Map.of(
                  "source",
                  "OpenFoodFacts",
                  "url",
                  url,
                  "confidence",
                  confidence,
                  "lastModified",
                  response.product.last_modified_t))
          .build();

    } catch (RestClientException e) {
      log.warn("Failed to lookup barcode {}: {}", barcode, e.getMessage());
      return BarcodeLookupResult.builder()
          .errorMessage("Service unavailable: " + e.getMessage())
          .metadata(Map.of("source", "OpenFoodFacts", "error", e.getClass().getSimpleName()))
          .build();
    } catch (Exception e) {
      log.error("Unexpected error looking up barcode {}: {}", barcode, e.getMessage(), e);
      return BarcodeLookupResult.builder()
          .errorMessage("Unexpected error: " + e.getMessage())
          .build();
    }
  }

  @Override
  public boolean isAvailable() {
    return enabled;
  }

  @Override
  public String getServiceName() {
    return "OpenFoodFacts";
  }

  @Override
  public List<String> getSupportedRegions() {
    return Arrays.asList("US", "EU", "CA", "AU", "GB", "FR", "DE", "IT", "ES", "NL");
  }

  /** Extract product information from OpenFoodFacts response */
  private ProductInfo extractProductInfo(OpenFoodFactsProduct product, String barcode) {
    String productName =
        product.product_name != null
            ? product.product_name
            : product.product_name_en != null
                ? product.product_name_en
                : product.product_name_fr != null ? product.product_name_fr : "Unknown Product";

    String brand = product.brands != null ? product.brands : "";
    String category = determineCategory(product);
    String ingredients =
        product.ingredients_text != null
            ? product.ingredients_text
            : product.ingredients_text_en != null ? product.ingredients_text_en : "";
    String allergens = product.allergens != null ? product.allergens : "";

    Map<String, Double> nutrition = extractNutrition(product);

    return new ProductInfo(
        productName,
        brand,
        category,
        ingredients,
        allergens,
        nutrition,
        product.image_url,
        "OpenFoodFacts",
        calculateConfidence(product));
  }

  /** Determine product category from OpenFoodFacts data */
  private String determineCategory(OpenFoodFactsProduct product) {
    if (product.categories_tags != null && !product.categories_tags.isEmpty()) {
      // Map OpenFoodFacts categories to our categories
      for (String tag : product.categories_tags) {
        if (tag.contains("snacks")) return "snacks";
        if (tag.contains("beverages")) return "beverages";
        if (tag.contains("dairy")) return "dairy";
        if (tag.contains("fruits")) return "fruits";
        if (tag.contains("vegetables")) return "vegetables";
        if (tag.contains("meat")) return "meat";
        if (tag.contains("seafood")) return "seafood";
        if (tag.contains("grains")) return "grains";
      }
    }
    return "other";
  }

  /** Extract nutrition information per 100g */
  private Map<String, Double> extractNutrition(OpenFoodFactsProduct product) {
    Map<String, Double> nutrition = new HashMap<>();

    if (product.nutriments == null) {
      return nutrition;
    }

    // Extract key nutrients (values are per 100g)
    if (product.nutriments.energy_kcal_100g != null) {
      nutrition.put("calories", product.nutriments.energy_kcal_100g);
    }
    if (product.nutriments.proteins_100g != null) {
      nutrition.put("protein", product.nutriments.proteins_100g);
    }
    if (product.nutriments.carbohydrates_100g != null) {
      nutrition.put("carbs", product.nutriments.carbohydrates_100g);
    }
    if (product.nutriments.fat_100g != null) {
      nutrition.put("fat", product.nutriments.fat_100g);
    }
    if (product.nutriments.fiber_100g != null) {
      nutrition.put("fiber", product.nutriments.fiber_100g);
    }
    if (product.nutriments.sugars_100g != null) {
      nutrition.put("sugars", product.nutriments.sugars_100g);
    }
    if (product.nutriments.sodium_100g != null) {
      nutrition.put("sodium", product.nutriments.sodium_100g);
    }

    return nutrition;
  }

  /** Calculate confidence score for the product data */
  private double calculateConfidence(OpenFoodFactsProduct product) {
    double confidence = 50.0; // Base confidence

    if (product.product_name != null && !product.product_name.trim().isEmpty()) {
      confidence += 15;
    }

    if (product.ingredients_text != null && !product.ingredients_text.trim().isEmpty()) {
      confidence += 15;
    }

    if (product.nutriments != null) {
      confidence += 10;
    }

    if (product.image_url != null && !product.image_url.trim().isEmpty()) {
      confidence += 5;
    }

    if (product.last_modified_t != null) {
      // More recently modified products are likely more accurate
      long daysSinceModified =
          (System.currentTimeMillis() / 1000 - product.last_modified_t) / 86400;
      if (daysSinceModified < 365) {
        confidence += 5;
      }
    }

    return Math.min(100.0, confidence);
  }

  /** Create mock result for testing when service is disabled */
  private BarcodeLookupResult createMockResult(String barcode) {
    Map<String, Double> mockNutrition =
        Map.of(
            "calories", 120.0,
            "protein", 2.5,
            "carbs", 28.0,
            "fat", 1.5,
            "fiber", 2.0,
            "sugars", 15.0);

    ProductInfo mockProduct =
        new ProductInfo(
            "Mock Product (Barcode: " + barcode + ")",
            "Mock Brand",
            "snacks",
            "Mock ingredients: sugar, flour, artificial flavors",
            "May contain nuts",
            mockNutrition,
            null,
            "OpenFoodFacts (Mock)",
            75.0);

    return BarcodeLookupResult.builder()
        .productInfo(mockProduct)
        .metadata(
            Map.of(
                "mock",
                true,
                "source",
                "OpenFoodFacts",
                "barcode",
                barcode,
                "note",
                "Using mock data - OpenFoodFacts disabled"))
        .build();
  }

  /** OpenFoodFacts API response classes */
  private static class OpenFoodFactsResponse {
    public int status;
    public String status_verbose;
    public OpenFoodFactsProduct product;
  }

  private static class OpenFoodFactsProduct {
    public String product_name;
    public String product_name_en;
    public String product_name_fr;
    public String brands;
    public String categories_tags;
    public String ingredients_text;
    public String ingredients_text_en;
    public String allergens;
    public String image_url;
    public Long last_modified_t;
    public Nutriments nutriments;
  }

  private static class Nutriments {
    public Double energy_kcal_100g;
    public Double proteins_100g;
    public Double carbohydrates_100g;
    public Double fat_100g;
    public Double fiber_100g;
    public Double sugars_100g;
    public Double sodium_100g;
  }
}
