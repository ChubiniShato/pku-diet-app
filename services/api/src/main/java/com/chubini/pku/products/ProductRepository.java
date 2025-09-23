package com.chubini.pku.products;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID> {
  // Fix: use productName instead of name
  Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);

  Page<Product> findByCategory(String category, Pageable pageable);

  @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
  List<String> findAllCategories();

  // Localized distinct categories with fallback to English
  @Query(
      """
    SELECT DISTINCT COALESCE(tReq.category, tEn.category, p.category)
    FROM Product p
    LEFT JOIN ProductTranslation tReq ON tReq.product = p AND tReq.locale = :lang
    LEFT JOIN ProductTranslation tEn ON tEn.product = p AND tEn.locale = 'en'
    ORDER BY COALESCE(tReq.category, tEn.category, p.category)
    """)
  List<String> findAllCategoriesLocalized(@Param("lang") String lang);

  @Query("SELECT p FROM Product p WHERE p.phenylalanine <= :maxPhe ORDER BY p.phenylalanine")
  Page<Product> findByMaxPhePer100g(@Param("maxPhe") Double maxPhe, Pageable pageable);

  // Find product by product code
  Optional<Product> findByProductCode(String productCode);

  // Localized product by ID with fallback to English
  @Query(
      """
    SELECT new com.chubini.pku.products.ProductDto(
      p.id, p.productCode,
      COALESCE(tReq.productName, tEn.productName, p.productName),
      COALESCE(tReq.category, tEn.category, p.category),
      p.phenylalanine, p.leucine, p.tyrosine, p.methionine,
      p.kilojoules, p.kilocalories, p.protein, p.carbohydrates, p.fats
    )
    FROM Product p
    LEFT JOIN ProductTranslation tReq ON tReq.product = p AND tReq.locale = :lang
    LEFT JOIN ProductTranslation tEn ON tEn.product = p AND tEn.locale = 'en'
    WHERE p.id = :id
    """)
  Optional<ProductDto> findByIdLocalized(@Param("lang") String lang, @Param("id") UUID id);

  // Localized product list with fallback to English
  @Query(
      """
    SELECT new com.chubini.pku.products.ProductDto(
      p.id, p.productCode,
      COALESCE(tReq.productName, tEn.productName, p.productName),
      COALESCE(tReq.category, tEn.category, p.category),
      p.phenylalanine, p.leucine, p.tyrosine, p.methionine,
      p.kilojoules, p.kilocalories, p.protein, p.carbohydrates, p.fats
    )
    FROM Product p
    LEFT JOIN ProductTranslation tReq ON tReq.product = p AND tReq.locale = :lang
    LEFT JOIN ProductTranslation tEn ON tEn.product = p AND tEn.locale = 'en'
    WHERE (:q IS NULL OR LOWER(COALESCE(tReq.productName, tEn.productName, p.productName)) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
  Page<ProductDto> findAllLocalized(
      @Param("lang") String lang, @Param("q") String q, Pageable pageable);

  // Localized product list by category with fallback to English
  @Query(
      """
    SELECT new com.chubini.pku.products.ProductDto(
      p.id, p.productCode,
      COALESCE(tReq.productName, tEn.productName, p.productName),
      COALESCE(tReq.category, tEn.category, p.category),
      p.phenylalanine, p.leucine, p.tyrosine, p.methionine,
      p.kilojoules, p.kilocalories, p.protein, p.carbohydrates, p.fats
    )
    FROM Product p
    LEFT JOIN ProductTranslation tReq ON tReq.product = p AND tReq.locale = :lang
    LEFT JOIN ProductTranslation tEn ON tEn.product = p AND tEn.locale = 'en'
    WHERE LOWER(COALESCE(tReq.category, tEn.category, p.category)) = LOWER(:category)
    """)
  Page<ProductDto> findByCategoryLocalized(
      @Param("lang") String lang, @Param("category") String category, Pageable pageable);

  // Localized products by category with search query and fallback to English
  @Query(
      """
    SELECT new com.chubini.pku.products.ProductDto(
      p.id, p.productCode,
      COALESCE(tReq.productName, tEn.productName, p.productName),
      COALESCE(tReq.category, tEn.category, p.category),
      p.phenylalanine, p.leucine, p.tyrosine, p.methionine,
      p.kilojoules, p.kilocalories, p.protein, p.carbohydrates, p.fats
    )
    FROM Product p
    LEFT JOIN ProductTranslation tReq ON tReq.product = p AND tReq.locale = :lang
    LEFT JOIN ProductTranslation tEn ON tEn.product = p AND tEn.locale = 'en'
    WHERE LOWER(COALESCE(tReq.category, tEn.category, p.category)) = LOWER(:category)
    AND (:q IS NULL OR LOWER(COALESCE(tReq.productName, tEn.productName, p.productName)) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
  Page<ProductDto> findByCategoryLocalized(
      @Param("lang") String lang,
      @Param("category") String category,
      @Param("q") String query,
      Pageable pageable);

  // Localized low PHE products with fallback to English
  @Query(
      """
    SELECT new com.chubini.pku.products.ProductDto(
      p.id, p.productCode,
      COALESCE(tReq.productName, tEn.productName, p.productName),
      COALESCE(tReq.category, tEn.category, p.category),
      p.phenylalanine, p.leucine, p.tyrosine, p.methionine,
      p.kilojoules, p.kilocalories, p.protein, p.carbohydrates, p.fats
    )
    FROM Product p
    LEFT JOIN ProductTranslation tReq ON tReq.product = p AND tReq.locale = :lang
    LEFT JOIN ProductTranslation tEn ON tEn.product = p AND tEn.locale = 'en'
    WHERE p.phenylalanine <= :maxPhe
    ORDER BY p.phenylalanine
    """)
  Page<ProductDto> findByMaxPhePer100gLocalized(
      @Param("lang") String lang, @Param("maxPhe") Double maxPhe, Pageable pageable);
}
