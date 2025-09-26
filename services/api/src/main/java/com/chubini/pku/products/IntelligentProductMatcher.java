package com.chubini.pku.products;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Intelligent product matching service that uses fuzzy matching algorithms to find products even
 * when names don't match exactly.
 *
 * <p>Features: - Exact matching (highest priority) - Fuzzy string matching using Levenshtein
 * distance - Synonym matching - Category-based suggestions - Multi-language support
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntelligentProductMatcher {

  private final ProductRepository productRepository;

  // Common food synonyms and variations
  private static final Map<String, List<String>> FOOD_SYNONYMS =
      Map.of(
          "bread", Arrays.asList("loaf", "bun", "roll", "toast", "slice"),
          "milk", Arrays.asList("dairy", "cream", "yogurt", "cheese"),
          "chicken", Arrays.asList("poultry", "hen", "breast", "thigh"),
          "beef", Arrays.asList("cow", "meat", "steak", "ground"),
          "rice", Arrays.asList("grain", "cereal", "white rice", "brown rice"),
          "potato", Arrays.asList("spud", "tuber", "fries", "chips"),
          "apple", Arrays.asList("fruit", "red apple", "green apple"),
          "carrot", Arrays.asList("vegetable", "orange", "root"),
          "tomato", Arrays.asList("red", "fruit", "cherry", "plum"),
          "onion", Arrays.asList("bulb", "yellow", "white", "red"));

  /**
   * Find products using intelligent matching
   *
   * @param searchName The name to search for
   * @param maxResults Maximum number of results to return
   * @return List of ProductMatchResult ordered by confidence score
   */
  public List<ProductMatchResult> findProductsIntelligently(String searchName, int maxResults) {
    if (searchName == null || searchName.trim().isEmpty()) {
      return Collections.emptyList();
    }

    String normalizedSearch = normalizeString(searchName);
    List<Product> allProducts = productRepository.findAll();

    List<ProductMatchResult> matches = new ArrayList<>();

    for (Product product : allProducts) {
      String productName = normalizeString(product.getProductName());
      double confidence = calculateMatchConfidence(normalizedSearch, productName, product);

      if (confidence > 0.1) { // Only include matches with >10% confidence
        matches.add(
            new ProductMatchResult(
                product, confidence, getMatchReason(normalizedSearch, productName)));
      }
    }

    // Sort by confidence (highest first) and return top results
    return matches.stream()
        .sorted((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()))
        .limit(maxResults)
        .collect(Collectors.toList());
  }

  /**
   * Find the best single product match
   *
   * @param searchName The name to search for
   * @return Optional containing the best match if found
   */
  public Optional<ProductMatchResult> findBestMatch(String searchName) {
    List<ProductMatchResult> matches = findProductsIntelligently(searchName, 1);
    return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
  }

  /** Calculate match confidence using multiple algorithms */
  private double calculateMatchConfidence(String searchName, String productName, Product product) {
    double exactMatch = calculateExactMatch(searchName, productName);
    if (exactMatch == 1.0) return 1.0;

    double fuzzyMatch = calculateFuzzyMatch(searchName, productName);
    double synonymMatch = calculateSynonymMatch(searchName, productName);
    double categoryMatch = calculateCategoryMatch(searchName, product);

    // Weighted combination of different matching strategies
    return Math.max(
        exactMatch, Math.max(fuzzyMatch * 0.8, Math.max(synonymMatch * 0.7, categoryMatch * 0.6)));
  }

  /** Exact match (case-insensitive) */
  private double calculateExactMatch(String searchName, String productName) {
    return searchName.equals(productName) ? 1.0 : 0.0;
  }

  /** Fuzzy string matching using Levenshtein distance */
  private double calculateFuzzyMatch(String searchName, String productName) {
    int maxLength = Math.max(searchName.length(), productName.length());
    if (maxLength == 0) return 0.0;

    int distance = levenshteinDistance(searchName, productName);
    return 1.0 - (double) distance / maxLength;
  }

  /** Synonym-based matching */
  private double calculateSynonymMatch(String searchName, String productName) {
    for (Map.Entry<String, List<String>> entry : FOOD_SYNONYMS.entrySet()) {
      String baseWord = entry.getKey();
      List<String> synonyms = entry.getValue();

      boolean searchContainsBase = searchName.contains(baseWord);
      boolean productContainsBase = productName.contains(baseWord);

      if (searchContainsBase && productContainsBase) {
        return 0.9; // High confidence for base word match
      }

      // Check synonyms
      for (String synonym : synonyms) {
        boolean searchContainsSynonym = searchName.contains(synonym);
        boolean productContainsSynonym = productName.contains(synonym);

        if ((searchContainsBase && productContainsSynonym)
            || (searchContainsSynonym && productContainsBase)) {
          return 0.8; // High confidence for synonym match
        }
      }
    }

    return 0.0;
  }

  /** Category-based matching */
  private double calculateCategoryMatch(String searchName, Product product) {
    if (product.getCategory() == null) return 0.0;

    String category = product.getCategory().toLowerCase();
    String normalizedSearch = searchName.toLowerCase();

    // Check if search term is contained in category
    if (category.contains(normalizedSearch) || normalizedSearch.contains(category)) {
      return 0.7;
    }

    // Check for common category keywords
    Map<String, List<String>> categoryKeywords =
        Map.of(
            "dairy", Arrays.asList("milk", "cheese", "yogurt", "cream"),
            "meat", Arrays.asList("chicken", "beef", "pork", "lamb"),
            "vegetables", Arrays.asList("carrot", "potato", "tomato", "onion"),
            "fruits", Arrays.asList("apple", "banana", "orange", "grape"),
            "grains", Arrays.asList("rice", "bread", "pasta", "cereal"));

    for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
      if (category.contains(entry.getKey())) {
        for (String keyword : entry.getValue()) {
          if (normalizedSearch.contains(keyword)) {
            return 0.6;
          }
        }
      }
    }

    return 0.0;
  }

  /** Calculate Levenshtein distance between two strings */
  private int levenshteinDistance(String s1, String s2) {
    int[][] dp = new int[s1.length() + 1][s2.length() + 1];

    for (int i = 0; i <= s1.length(); i++) {
      for (int j = 0; j <= s2.length(); j++) {
        if (i == 0) {
          dp[i][j] = j;
        } else if (j == 0) {
          dp[i][j] = i;
        } else {
          dp[i][j] =
              Math.min(
                  Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                  dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1));
        }
      }
    }

    return dp[s1.length()][s2.length()];
  }

  /** Normalize string for comparison */
  private String normalizeString(String input) {
    return input
        .toLowerCase()
        .replaceAll("[^a-z0-9\\s]", "") // Remove special characters
        .replaceAll("\\s+", " ") // Normalize whitespace
        .trim();
  }

  /** Get human-readable match reason */
  private String getMatchReason(String searchName, String productName) {
    if (searchName.equals(productName)) {
      return "Exact match";
    } else if (productName.contains(searchName)) {
      return "Product name contains search term";
    } else if (searchName.contains(productName)) {
      return "Search term contains product name";
    } else {
      return "Fuzzy match";
    }
  }

  /** Result class for product matches */
  public static class ProductMatchResult {
    private final Product product;
    private final double confidence;
    private final String matchReason;

    public ProductMatchResult(Product product, double confidence, String matchReason) {
      this.product = product;
      this.confidence = confidence;
      this.matchReason = matchReason;
    }

    public Product getProduct() {
      return product;
    }

    public double getConfidence() {
      return confidence;
    }

    public String getMatchReason() {
      return matchReason;
    }

    public boolean isHighConfidence() {
      return confidence >= 0.8;
    }

    public boolean isMediumConfidence() {
      return confidence >= 0.5 && confidence < 0.8;
    }

    public boolean isLowConfidence() {
      return confidence < 0.5;
    }
  }
}
