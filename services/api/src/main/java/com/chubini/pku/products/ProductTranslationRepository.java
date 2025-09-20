package com.chubini.pku.products;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductTranslationRepository extends JpaRepository<ProductTranslation, Long> {

  /** Find translation by product ID and locale */
  Optional<ProductTranslation> findByProductIdAndLocale(UUID productId, String locale);

  /** Find all translations for given product codes and locale */
  @Query(
      "SELECT t FROM ProductTranslation t WHERE t.product.productCode IN :codes AND t.locale = :locale")
  List<ProductTranslation> findAllByCodesAndLocale(
      @Param("codes") Collection<String> codes, @Param("locale") String locale);

  /** Find all translations for a specific product */
  List<ProductTranslation> findByProductId(UUID productId);

  /** Find all translations for a specific locale */
  List<ProductTranslation> findByLocale(String locale);

  /** Check if translation exists for product and locale */
  boolean existsByProductIdAndLocale(UUID productId, String locale);

  /** Delete translations by product ID and locale */
  void deleteByProductIdAndLocale(UUID productId, String locale);
}
