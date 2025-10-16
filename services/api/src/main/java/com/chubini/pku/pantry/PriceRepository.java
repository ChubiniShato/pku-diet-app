package com.chubini.pku.pantry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chubini.pku.products.CustomProduct;
import com.chubini.pku.products.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends JpaRepository<PriceEntry, UUID> {

  /** Find current prices for a product */
  List<PriceEntry> findByProductAndIsCurrentTrueOrderByRecordedDateDesc(Product product);

  /** Find current prices for a custom product */
  List<PriceEntry> findByCustomProductAndIsCurrentTrueOrderByRecordedDateDesc(
      CustomProduct customProduct);

  /** Find current prices by region */
  List<PriceEntry> findByRegionAndIsCurrentTrueOrderByPricePerUnitAsc(String region);

  /** Find best current price for a product */
  @Query(
      "SELECT pe FROM PriceEntry pe WHERE pe.product = :product "
          + "AND pe.isCurrent = true "
          + "ORDER BY pe.pricePerUnit ASC")
  Optional<PriceEntry> findBestPriceForProduct(@Param("product") Product product);

  /** Find best current price for a custom product */
  @Query(
      "SELECT pe FROM PriceEntry pe WHERE pe.customProduct = :customProduct "
          + "AND pe.isCurrent = true "
          + "ORDER BY pe.pricePerUnit ASC")
  Optional<PriceEntry> findBestPriceForCustomProduct(
      @Param("customProduct") CustomProduct customProduct);

  /** Find prices recorded within date range */
  @Query(
      "SELECT pe FROM PriceEntry pe WHERE pe.recordedDate BETWEEN :startDate AND :endDate "
          + "ORDER BY pe.recordedDate DESC")
  List<PriceEntry> findByDateRange(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  /** Find average price for a product in a region */
  @Query(
      "SELECT AVG(pe.pricePerUnit) FROM PriceEntry pe "
          + "WHERE pe.product = :product AND pe.region = :region "
          + "AND pe.isCurrent = true")
  Optional<Double> getAveragePriceForProductInRegion(
      @Param("product") Product product, @Param("region") String region);

  /** Find recent prices (within last 30 days) */
  @Query(
      "SELECT pe FROM PriceEntry pe WHERE pe.recordedDate >= :cutoffDate "
          + "ORDER BY pe.recordedDate DESC")
  List<PriceEntry> findRecentPrices(@Param("cutoffDate") LocalDate cutoffDate);

  /** Find prices by store */
  List<PriceEntry> findByStoreNameAndIsCurrentTrueOrderByPricePerUnitAsc(String storeName);

  /** Find cheapest alternatives for a product category */
  @Query(
      "SELECT pe FROM PriceEntry pe "
          + "WHERE pe.isCurrent = true "
          + "AND ((pe.itemType = 'PRODUCT' AND pe.product.category = :category) "
          + "OR (pe.itemType = 'CUSTOM_PRODUCT' AND pe.customProduct.category = :category)) "
          + "ORDER BY pe.pricePerUnit ASC")
  List<PriceEntry> findCheapestInCategory(@Param("category") String category);
}
