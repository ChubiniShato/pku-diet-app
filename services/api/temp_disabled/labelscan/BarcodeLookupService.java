package com.chubini.pku.labelscan;

import java.util.Map;

/** Barcode lookup service interface for finding product information by barcode */
public interface BarcodeLookupService {

  /** Lookup product by barcode */
  BarcodeLookupResult lookupByBarcode(String barcode, String region);

  /** Check if service is available */
  boolean isAvailable();

  /** Get service name */
  String getServiceName();

  /** Get supported regions */
  java.util.List<String> getSupportedRegions();

  /** Result of barcode lookup */
  class BarcodeLookupResult {
    private final boolean found;
    private final ProductInfo productInfo;
    private final String errorMessage;
    private final Map<String, Object> metadata;

    public BarcodeLookupResult(
        boolean found, ProductInfo productInfo, String errorMessage, Map<String, Object> metadata) {
      this.found = found;
      this.productInfo = productInfo;
      this.errorMessage = errorMessage;
      this.metadata = metadata;
    }

    public static Builder builder() {
      return new Builder();
    }

    // Getters
    public boolean isFound() {
      return found;
    }

    public ProductInfo getProductInfo() {
      return productInfo;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public Map<String, Object> getMetadata() {
      return metadata;
    }

    public static class Builder {
      private boolean found = false;
      private ProductInfo productInfo;
      private String errorMessage;
      private Map<String, Object> metadata;

      public Builder found(boolean found) {
        this.found = found;
        return this;
      }

      public Builder productInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
        this.found = productInfo != null;
        return this;
      }

      public Builder errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
      }

      public Builder metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
      }

      public BarcodeLookupResult build() {
        return new BarcodeLookupResult(found, productInfo, errorMessage, metadata);
      }
    }
  }

  /** Product information from barcode lookup */
  class ProductInfo {
    private final String productName;
    private final String brand;
    private final String category;
    private final String ingredients;
    private final String allergens;
    private final Map<String, Double> nutritionPer100g;
    private final String imageUrl;
    private final String source;
    private final Double confidence;

    public ProductInfo(
        String productName,
        String brand,
        String category,
        String ingredients,
        String allergens,
        Map<String, Double> nutritionPer100g,
        String imageUrl,
        String source,
        Double confidence) {
      this.productName = productName;
      this.brand = brand;
      this.category = category;
      this.ingredients = ingredients;
      this.allergens = allergens;
      this.nutritionPer100g = nutritionPer100g;
      this.imageUrl = imageUrl;
      this.source = source;
      this.confidence = confidence;
    }

    // Getters
    public String getProductName() {
      return productName;
    }

    public String getBrand() {
      return brand;
    }

    public String getCategory() {
      return category;
    }

    public String getIngredients() {
      return ingredients;
    }

    public String getAllergens() {
      return allergens;
    }

    public Map<String, Double> getNutritionPer100g() {
      return nutritionPer100g;
    }

    public String getImageUrl() {
      return imageUrl;
    }

    public String getSource() {
      return source;
    }

    public Double getConfidence() {
      return confidence;
    }
  }
}
