package com.chubini.pku.products;

import java.util.List;
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

  @Query("SELECT p FROM Product p WHERE p.phenylalanine <= :maxPhe ORDER BY p.phenylalanine")
  Page<Product> findByMaxPhePer100g(@Param("maxPhe") Double maxPhe, Pageable pageable);
}
