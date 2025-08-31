package com.chubini.pku.labelscan;

import java.util.*;
import java.util.regex.Pattern;

import com.chubini.pku.patients.PatientProfile;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for flagging safety concerns in food labels based on patient allergies and restrictions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SafetyFlaggingService {

  // Common allergen patterns (case-insensitive)
  private static final Map<String, Pattern> ALLERGEN_PATTERNS =
      Map.of(
          "nuts",
              Pattern.compile(
                  "\\b(nuts?|peanuts?|almonds?|walnuts?|cashews?|pistachios?|hazelnuts?|pecans?|brazil\\s+nuts?|macadamia)\\b",
                  Pattern.CASE_INSENSITIVE),
          "dairy",
              Pattern.compile(
                  "\\b(dairy|milk|cheese|yogurt|butter|cream|casein|lactose|whey)\\b",
                  Pattern.CASE_INSENSITIVE),
          "eggs",
              Pattern.compile(
                  "\\b(eggs?|egg\\s+protein|albumin|globulin)\\b", Pattern.CASE_INSENSITIVE),
          "fish",
              Pattern.compile(
                  "\\b(fish|shellfish|shrimp|crab|lobster|mussels|oysters|salmon|tuna|cod)\\b",
                  Pattern.CASE_INSENSITIVE),
          "soy",
              Pattern.compile(
                  "\\b(soy|soya|soybean|soy\\s+protein|soy\\s+lecithin)\\b",
                  Pattern.CASE_INSENSITIVE),
          "wheat",
              Pattern.compile("\\b(wheat|gluten|barley|rye|oats)\\b", Pattern.CASE_INSENSITIVE),
          "corn",
              Pattern.compile(
                  "\\b(corn|maize|corn\\s+syrup|corn\\s+starch)\\b", Pattern.CASE_INSENSITIVE),
          "sesame", Pattern.compile("\\b(sesame|tahini)\\b", Pattern.CASE_INSENSITIVE),
          "mustard", Pattern.compile("\\b(mustard)\\b", Pattern.CASE_INSENSITIVE));

  // Common forbidden ingredient patterns for PKU
  private static final Map<String, Pattern> FORBIDDEN_PATTERNS =
      Map.of(
          "aspartame", Pattern.compile("\\b(aspartame)\\b", Pattern.CASE_INSENSITIVE),
          "phenylalanine", Pattern.compile("\\b(phenylalanine|phe)\\b", Pattern.CASE_INSENSITIVE),
          "artificial_sweeteners",
              Pattern.compile(
                  "\\b(saccharin|cyclamate|acesulfame|sucralose|stevia)\\b",
                  Pattern.CASE_INSENSITIVE),
          "high_fructose_corn_syrup",
              Pattern.compile(
                  "\\b(high\\s+fructose\\s+corn\\s+syrup|hfcs)\\b", Pattern.CASE_INSENSITIVE),
          "trans_fat",
              Pattern.compile(
                  "\\b(trans\\s+fat|partially\\s+hydrogenated|hydrogenated\\s+oil)\\b",
                  Pattern.CASE_INSENSITIVE),
          "artificial_colors",
              Pattern.compile(
                  "\\b(red\\s+\\d+|blue\\s+\\d+|yellow\\s+\\d+|artificial\\s+color|fd&c)\\b",
                  Pattern.CASE_INSENSITIVE),
          "artificial_flavors",
              Pattern.compile(
                  "\\b(artificial\\s+flavor|natural\\s+flavor|\\bflavorings\\b)\\b",
                  Pattern.CASE_INSENSITIVE),
          "preservatives",
              Pattern.compile(
                  "\\b(bha|bht|tbhq|sodium\\s+benzoate|potassium\\s+sorbate|calcium\\s+propionate)\\b",
                  Pattern.CASE_INSENSITIVE));

  // Severity levels for different types of concerns
  public enum SafetyLevel {
    SAFE, // No concerns detected
    WARNING, // Minor concerns or unclear labeling
    DANGER, // Major allergen or forbidden ingredient detected
    CRITICAL // Multiple major concerns or high-risk ingredients
  }

  /** Analyze text for safety concerns based on patient profile */
  public SafetyAnalysis analyzeText(String text, PatientProfile patient) {
    if (text == null || text.trim().isEmpty()) {
      return SafetyAnalysis.safe();
    }

    List<String> allergenHits = new ArrayList<>();
    List<String> forbiddenHits = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    SafetyLevel overallLevel = SafetyLevel.SAFE;

    // Check for allergens
    if (patient.getAllergens() != null && !patient.getAllergens().isEmpty()) {
      for (String allergen : patient.getAllergens()) {
        Pattern pattern = ALLERGEN_PATTERNS.get(allergen.toLowerCase());
        if (pattern != null && pattern.matcher(text).find()) {
          allergenHits.add(allergen);
          overallLevel = SafetyLevel.DANGER;
          log.warn("Allergen detected: {} in text", allergen);
        }
      }
    }

    // Check for forbidden ingredients
    for (Map.Entry<String, Pattern> entry : FORBIDDEN_PATTERNS.entrySet()) {
      if (entry.getValue().matcher(text).find()) {
        forbiddenHits.add(entry.getKey());
        if (overallLevel != SafetyLevel.DANGER) {
          overallLevel = SafetyLevel.WARNING;
        }
        log.info("Forbidden ingredient pattern detected: {}", entry.getKey());
      }
    }

    // Check for unclear or suspicious labeling
    addLabelingWarnings(text, warnings);

    // Upgrade to CRITICAL if multiple concerns
    if (allergenHits.size() > 1 || (allergenHits.size() > 0 && forbiddenHits.size() > 0)) {
      overallLevel = SafetyLevel.CRITICAL;
    }

    return new SafetyAnalysis(
        overallLevel,
        allergenHits,
        forbiddenHits,
        warnings,
        calculateConfidence(text, allergenHits, forbiddenHits));
  }

  /** Analyze product information from barcode lookup */
  public SafetyAnalysis analyzeProductInfo(
      BarcodeLookupService.ProductInfo productInfo, PatientProfile patient) {
    List<String> allergenHits = new ArrayList<>();
    List<String> forbiddenHits = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    SafetyLevel overallLevel = SafetyLevel.SAFE;

    // Analyze ingredients text
    if (productInfo.getIngredients() != null) {
      SafetyAnalysis ingredientsAnalysis = analyzeText(productInfo.getIngredients(), patient);
      allergenHits.addAll(ingredientsAnalysis.getAllergenHits());
      forbiddenHits.addAll(ingredientsAnalysis.getForbiddenHits());
      warnings.addAll(ingredientsAnalysis.getWarnings());
      if (ingredientsAnalysis.getOverallLevel().ordinal() > overallLevel.ordinal()) {
        overallLevel = ingredientsAnalysis.getOverallLevel();
      }
    }

    // Analyze allergens field
    if (productInfo.getAllergens() != null) {
      SafetyAnalysis allergensAnalysis = analyzeText(productInfo.getAllergens(), patient);
      allergenHits.addAll(allergensAnalysis.getAllergenHits());
      if (allergensAnalysis.getOverallLevel() == SafetyLevel.DANGER) {
        overallLevel = SafetyLevel.DANGER;
      }
    }

    // Check nutrition data for concerns
    if (productInfo.getNutritionPer100g() != null) {
      addNutritionWarnings(productInfo.getNutritionPer100g(), warnings);
    }

    return new SafetyAnalysis(
        overallLevel,
        allergenHits,
        forbiddenHits,
        warnings,
        productInfo.getConfidence() != null ? productInfo.getConfidence() : 50.0);
  }

  /** Add warnings for suspicious or unclear labeling */
  private void addLabelingWarnings(String text, List<String> warnings) {
    String lowerText = text.toLowerCase();

    // Check for vague terms
    if (lowerText.contains("natural flavors") && !lowerText.contains("no artificial")) {
      warnings.add("Contains 'natural flavors' - may include allergens");
    }

    // Check for "may contain" statements
    if (lowerText.contains("may contain")) {
      warnings.add("Contains 'may contain' statements - potential allergen risk");
    }

    // Check for unclear ingredient lists
    if (lowerText.contains("and/or") || lowerText.contains("spices")) {
      warnings.add("Unclear ingredient specifications");
    }

    // Check for very short ingredient lists (may be incomplete)
    String ingredientsSection = extractIngredientsSection(text);
    if (ingredientsSection != null && ingredientsSection.length() < 50) {
      warnings.add("Ingredient list appears incomplete");
    }
  }

  /** Add warnings based on nutrition data */
  private void addNutritionWarnings(Map<String, Double> nutrition, List<String> warnings) {
    // High sugar content
    Double sugars = nutrition.get("sugars");
    if (sugars != null && sugars > 20.0) {
      warnings.add("High sugar content per 100g");
    }

    // High sodium content
    Double sodium = nutrition.get("sodium");
    if (sodium != null && sodium > 1.0) { // More than 1g per 100g
      warnings.add("High sodium content");
    }

    // Very high calorie density
    Double calories = nutrition.get("calories");
    if (calories != null && calories > 500) {
      warnings.add("Very high calorie density");
    }
  }

  /** Extract ingredients section from OCR text */
  private String extractIngredientsSection(String text) {
    String lowerText = text.toLowerCase();

    // Look for common section headers
    String[] headers = {"ingredients", "ingredients:", "contains", "contains:"};
    for (String header : headers) {
      int index = lowerText.indexOf(header);
      if (index != -1) {
        // Extract text after the header, up to next section or end
        String afterHeader = text.substring(index + header.length()).trim();
        int nextSection = findNextSection(afterHeader);
        return nextSection != -1 ? afterHeader.substring(0, nextSection) : afterHeader;
      }
    }

    return null;
  }

  /** Find the start of the next section in the text */
  private int findNextSection(String text) {
    String[] sectionHeaders = {"nutrition", "allergens", "serving", "directions", "warnings"};
    String lowerText = text.toLowerCase();

    for (String header : sectionHeaders) {
      int index = lowerText.indexOf(header);
      if (index != -1) {
        return index;
      }
    }

    return -1;
  }

  /** Calculate confidence in the safety analysis */
  private double calculateConfidence(
      String text, List<String> allergenHits, List<String> forbiddenHits) {
    double confidence = 70.0; // Base confidence

    // Higher confidence if we found clear matches
    if (!allergenHits.isEmpty() || !forbiddenHits.isEmpty()) {
      confidence += 20;
    }

    // Lower confidence for short or unclear text
    if (text.length() < 100) {
      confidence -= 15;
    }

    // Lower confidence if text contains many unclear terms
    long unclearTerms = countUnclearTerms(text);
    confidence -= (unclearTerms * 2);

    return Math.max(0.0, Math.min(100.0, confidence));
  }

  /** Count unclear or suspicious terms in the text */
  private long countUnclearTerms(String text) {
    String[] unclearPatterns = {"proprietary", "confidential", "trade secret", "various", "other"};
    long count = 0;

    String lowerText = text.toLowerCase();
    for (String pattern : unclearPatterns) {
      if (lowerText.contains(pattern)) {
        count++;
      }
    }

    return count;
  }

  /** Get predefined allergen patterns for reference */
  public Set<String> getSupportedAllergens() {
    return ALLERGEN_PATTERNS.keySet();
  }

  /** Get predefined forbidden ingredient patterns */
  public Set<String> getSupportedForbiddenPatterns() {
    return FORBIDDEN_PATTERNS.keySet();
  }

  /** Safety analysis result */
  public static class SafetyAnalysis {
    private final SafetyLevel overallLevel;
    private final List<String> allergenHits;
    private final List<String> forbiddenHits;
    private final List<String> warnings;
    private final double confidence;

    public SafetyAnalysis(
        SafetyLevel overallLevel,
        List<String> allergenHits,
        List<String> forbiddenHits,
        List<String> warnings,
        double confidence) {
      this.overallLevel = overallLevel;
      this.allergenHits = allergenHits != null ? new ArrayList<>(allergenHits) : new ArrayList<>();
      this.forbiddenHits =
          forbiddenHits != null ? new ArrayList<>(forbiddenHits) : new ArrayList<>();
      this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
      this.confidence = confidence;
    }

    public static SafetyAnalysis safe() {
      return new SafetyAnalysis(
          SafetyLevel.SAFE, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 100.0);
    }

    // Getters
    public SafetyLevel getOverallLevel() {
      return overallLevel;
    }

    public List<String> getAllergenHits() {
      return allergenHits;
    }

    public List<String> getForbiddenHits() {
      return forbiddenHits;
    }

    public List<String> getWarnings() {
      return warnings;
    }

    public double getConfidence() {
      return confidence;
    }

    public boolean hasSafetyConcerns() {
      return overallLevel != SafetyLevel.SAFE;
    }

    public int getTotalConcernCount() {
      return allergenHits.size() + forbiddenHits.size() + warnings.size();
    }
  }
}
