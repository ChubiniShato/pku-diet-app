package com.chubini.pku.api.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import java.util.List;

public interface FoodProductRepository extends JpaRepository<FoodProduct, UUID> {
    Page<FoodProduct> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<FoodProduct> findByCategory(String category, Pageable pageable);
    
    Page<FoodProduct> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<FoodProduct> findByCategoryAndIsActive(String category, Boolean isActive, Pageable pageable);
    
    @Query("SELECT DISTINCT f.category FROM FoodProduct f WHERE f.isActive = true ORDER BY f.category")
    List<String> findAllActiveCategories();
    
    @Query("SELECT f FROM FoodProduct f WHERE f.phePer100g <= :maxPhe AND f.isActive = true ORDER BY f.phePer100g")
    Page<FoodProduct> findByMaxPhePer100g(@Param("maxPhe") Double maxPhe, Pageable pageable);
}
