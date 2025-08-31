package com.chubini.pku.labelscan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.chubini.pku.patients.PatientProfile;
import com.chubini.pku.products.Product;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

/** LabelScanSubmission entity for processing food label images */
@Entity
@Table(
    name = "label_scan_submission",
    indexes = {
      @Index(name = "idx_label_scan_patient", columnList = "patient_id"),
      @Index(name = "idx_label_scan_status", columnList = "status"),
      @Index(name = "idx_label_scan_barcode", columnList = "barcode"),
      @Index(name = "idx_label_scan_created", columnList = "created_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabelScanSubmission {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  private PatientProfile patient;

  @ElementCollection
  @CollectionTable(name = "label_scan_images", joinColumns = @JoinColumn(name = "submission_id"))
  @Column(name = "image_path")
  private List<String> imagePaths; // Paths to stored images

  @Column(name = "barcode")
  private String barcode; // Provided or extracted barcode

  @Column(name = "region", length = 10)
  private String region; // ISO country code (e.g., "US", "GE", "RU")

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private ScanStatus status = ScanStatus.PENDING;

  @Column(name = "ocr_text", columnDefinition = "TEXT")
  private String ocrText; // Full OCR text extraction

  @Column(name = "ocr_confidence")
  private Double ocrConfidence; // OCR confidence score (0.0-1.0)

  @ElementCollection
  @CollectionTable(
      name = "label_scan_extracted_fields",
      joinColumns = @JoinColumn(name = "submission_id"))
  @MapKeyColumn(name = "field_name")
  @Column(name = "field_value")
  private Map<String, String> extractedFields; // Key-value pairs: ingredients, nutrition, allergens

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "matched_product_id")
  private Product matchedProduct; // Product matched via barcode or OCR

  @Column(name = "match_confidence")
  private Double matchConfidence; // Confidence of the product match

  @Column(name = "match_source")
  private String matchSource; // BARCODE, OCR_NAME, MANUAL

  @ElementCollection
  @CollectionTable(
      name = "label_scan_allergen_hits",
      joinColumns = @JoinColumn(name = "submission_id"))
  @Column(name = "allergen")
  private List<String> allergenHits; // Detected allergens from patient's profile

  @ElementCollection
  @CollectionTable(
      name = "label_scan_forbidden_hits",
      joinColumns = @JoinColumn(name = "submission_id"))
  @Column(name = "ingredient")
  private List<String> forbiddenHits; // Detected forbidden ingredients

  @ElementCollection
  @CollectionTable(name = "label_scan_warnings", joinColumns = @JoinColumn(name = "submission_id"))
  @Column(name = "warning")
  private List<String> warnings; // General warnings (e.g., "Unclear text", "Incomplete label")

  @Column(name = "review_notes", columnDefinition = "TEXT")
  private String reviewNotes; // Notes from manual review

  @Column(name = "reviewed_by")
  private String reviewedBy; // User/device that reviewed

  @Column(name = "reviewed_at")
  private LocalDateTime reviewedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "processed_at")
  private LocalDateTime processedAt;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  /** Scan status enumeration */
  public enum ScanStatus {
    PENDING, // Initial state, processing not started
    PROCESSING, // OCR and barcode lookup in progress
    MATCHED, // Successfully matched to existing product
    REQUIRES_REVIEW, // Needs manual review (no match, unclear OCR, etc.)
    APPROVED, // Manually reviewed and approved
    REJECTED, // Manually reviewed and rejected
    FAILED // Processing failed (OCR error, etc.)
  }

  /** Check if scan has any safety concerns */
  public boolean hasSafetyConcerns() {
    return (allergenHits != null && !allergenHits.isEmpty())
        || (forbiddenHits != null && !forbiddenHits.isEmpty());
  }

  /** Check if scan requires manual review */
  public boolean requiresReview() {
    return status == ScanStatus.REQUIRES_REVIEW
        || status == ScanStatus.FAILED
        || hasSafetyConcerns();
  }

  /** Get the total number of safety flags */
  public int getSafetyFlagCount() {
    int count = 0;
    if (allergenHits != null) count += allergenHits.size();
    if (forbiddenHits != null) count += forbiddenHits.size();
    return count;
  }

  /** Mark as processed with result */
  public void markProcessed(
      ScanStatus newStatus, String matchSource, Product matchedProduct, Double matchConfidence) {
    this.status = newStatus;
    this.matchSource = matchSource;
    this.matchedProduct = matchedProduct;
    this.matchConfidence = matchConfidence;
    this.processedAt = LocalDateTime.now();
  }

  /** Mark as failed with error message */
  public void markFailed(String errorMessage) {
    this.status = ScanStatus.FAILED;
    this.errorMessage = errorMessage;
    this.processedAt = LocalDateTime.now();
  }

  /** Add allergen hit */
  public void addAllergenHit(String allergen) {
    if (this.allergenHits == null) {
      this.allergenHits = new java.util.ArrayList<>();
    }
    if (!this.allergenHits.contains(allergen)) {
      this.allergenHits.add(allergen);
    }
  }

  /** Add forbidden ingredient hit */
  public void addForbiddenHit(String ingredient) {
    if (this.forbiddenHits == null) {
      this.forbiddenHits = new java.util.ArrayList<>();
    }
    if (!this.forbiddenHits.contains(ingredient)) {
      this.forbiddenHits.add(ingredient);
    }
  }

  /** Add warning */
  public void addWarning(String warning) {
    if (this.warnings == null) {
      this.warnings = new java.util.ArrayList<>();
    }
    if (!this.warnings.contains(warning)) {
      this.warnings.add(warning);
    }
  }

  /** Mark as reviewed */
  public void markReviewed(String reviewedBy, String notes) {
    this.reviewedBy = reviewedBy;
    this.reviewNotes = notes;
    this.reviewedAt = LocalDateTime.now();
  }
}
