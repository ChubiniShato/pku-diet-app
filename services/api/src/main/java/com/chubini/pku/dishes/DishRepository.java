package com.chubini.pku.dishes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<Dish, UUID> {

    /**
     * Find dishes by name containing search query (case-insensitive)
     */
    @Query("SELECT d FROM Dish d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Dish> findByNameContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    /**
     * Find dishes by category
     */
    Page<Dish> findByCategory(String category, Pageable pageable);

    /**
     * Find dishes by category containing search query
     */
    @Query("SELECT d FROM Dish d WHERE d.category = :category AND LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Dish> findByCategoryAndNameContainingIgnoreCase(@Param("category") String category, 
                                                         @Param("query") String query, 
                                                         Pageable pageable);

    /**
     * Find dishes with low phenylalanine per 100g
     */
    @Query("SELECT d FROM Dish d WHERE d.per100Phenylalanine <= :maxPhe")
    Page<Dish> findByLowPhenylalanine(@Param("maxPhe") BigDecimal maxPhe, Pageable pageable);

    /**
     * Get all distinct categories
     */
    @Query("SELECT DISTINCT d.category FROM Dish d WHERE d.category IS NOT NULL ORDER BY d.category")
    List<String> findAllCategories();

    /**
     * Find dish with items (fetch join to avoid N+1 queries)
     */
    @Query("SELECT d FROM Dish d LEFT JOIN FETCH d.items WHERE d.id = :id")
    Optional<Dish> findByIdWithItems(@Param("id") UUID id);

    /**
     * Find dishes with high protein per 100g
     */
    @Query("SELECT d FROM Dish d WHERE d.per100Protein >= :minProtein")
    Page<Dish> findByHighProtein(@Param("minProtein") BigDecimal minProtein, Pageable pageable);

    /**
     * Find dishes within calorie range per 100g
     */
    @Query("SELECT d FROM Dish d WHERE d.per100Kilocalories BETWEEN :minCalories AND :maxCalories")
    Page<Dish> findByCalorieRange(@Param("minCalories") BigDecimal minCalories, 
                                  @Param("maxCalories") BigDecimal maxCalories, 
                                  Pageable pageable);

    /**
     * Count dishes by category
     */
    @Query("SELECT COUNT(d) FROM Dish d WHERE d.category = :category")
    long countByCategory(@Param("category") String category);
}
