package com.chubini.pku.pantry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chubini.pku.patients.PatientProfile;
import com.chubini.pku.products.CustomProduct;
import com.chubini.pku.products.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PantryRepository extends JpaRepository<PantryItem, UUID> {

  /** Find all available pantry items for a patient */
  List<PantryItem> findByPatientAndIsAvailableTrueOrderByExpiryDateAsc(PatientProfile patient);

  /** Find available pantry items for a specific product */
  List<PantryItem> findByPatientAndProductAndIsAvailableTrueOrderByExpiryDateAsc(
      PatientProfile patient, Product product);

  /** Find available pantry items for a specific custom product */
  List<PantryItem> findByPatientAndCustomProductAndIsAvailableTrueOrderByExpiryDateAsc(
      PatientProfile patient, CustomProduct customProduct);

  /** Find pantry items expiring soon */
  @Query(
      "SELECT pi FROM PantryItem pi WHERE pi.patient = :patient "
          + "AND pi.isAvailable = true "
          + "AND pi.expiryDate IS NOT NULL "
          + "AND pi.expiryDate BETWEEN :now AND :futureDate "
          + "ORDER BY pi.expiryDate ASC")
  List<PantryItem> findExpiringSoon(
      @Param("patient") PatientProfile patient,
      @Param("now") LocalDate now,
      @Param("futureDate") LocalDate futureDate);

  /** Find expired pantry items */
  @Query(
      "SELECT pi FROM PantryItem pi WHERE pi.patient = :patient "
          + "AND pi.isAvailable = true "
          + "AND pi.expiryDate IS NOT NULL "
          + "AND pi.expiryDate < :now "
          + "ORDER BY pi.expiryDate ASC")
  List<PantryItem> findExpired(
      @Param("patient") PatientProfile patient, @Param("now") LocalDate now);

  /** Find pantry items by location */
  List<PantryItem> findByPatientAndLocationAndIsAvailableTrueOrderByExpiryDateAsc(
      PatientProfile patient, String location);

  /** Get total quantity available for a product */
  @Query(
      "SELECT COALESCE(SUM(pi.quantityGrams), 0) FROM PantryItem pi "
          + "WHERE pi.patient = :patient AND pi.product = :product AND pi.isAvailable = true")
  Optional<java.math.BigDecimal> getTotalQuantityForProduct(
      @Param("patient") PatientProfile patient, @Param("product") Product product);

  /** Get total quantity available for a custom product */
  @Query(
      "SELECT COALESCE(SUM(pi.quantityGrams), 0) FROM PantryItem pi "
          + "WHERE pi.patient = :patient AND pi.customProduct = :customProduct AND pi.isAvailable = true")
  Optional<java.math.BigDecimal> getTotalQuantityForCustomProduct(
      @Param("patient") PatientProfile patient,
      @Param("customProduct") CustomProduct customProduct);

  /** Find items by category (through product/custom product) */
  @Query(
      "SELECT pi FROM PantryItem pi "
          + "WHERE pi.patient = :patient AND pi.isAvailable = true "
          + "AND ((pi.itemType = 'PRODUCT' AND pi.product.category = :category) "
          + "OR (pi.itemType = 'CUSTOM_PRODUCT' AND pi.customProduct.category = :category)) "
          + "ORDER BY pi.expiryDate ASC")
  List<PantryItem> findByPatientAndCategoryAndIsAvailableTrue(
      @Param("patient") PatientProfile patient, @Param("category") String category);

  /** Count available items for a patient */
  long countByPatientAndIsAvailableTrue(PatientProfile patient);

  /** Find low stock items (less than specified quantity) */
  @Query(
      "SELECT pi FROM PantryItem pi WHERE pi.patient = :patient "
          + "AND pi.isAvailable = true "
          + "AND pi.quantityGrams < :threshold "
          + "ORDER BY pi.quantityGrams ASC")
  List<PantryItem> findLowStockItems(
      @Param("patient") PatientProfile patient, @Param("threshold") java.math.BigDecimal threshold);
}
