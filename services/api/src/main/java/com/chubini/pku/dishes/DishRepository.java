package com.chubini.pku.dishes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<Dish, UUID> {

    Page<Dish> findByIsVisibleTrueOrderByName(Pageable pageable);

    Page<Dish> findByCategoryAndIsVisibleTrue(String category, Pageable pageable);

    @Query("SELECT d FROM Dish d WHERE d.isVisible = true AND d.name ILIKE %:searchTerm%")
    Page<Dish> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT DISTINCT d.category FROM Dish d WHERE d.isVisible = true AND d.category IS NOT NULL")
    List<String> findDistinctCategories();

    @Query("SELECT d FROM Dish d WHERE d.isVisible = true AND d.per100Phenylalanine <= :maxPhe")
    Page<Dish> findLowPheDishes(@Param("maxPhe") Double maxPhe, Pageable pageable);

    List<Dish> findByDifficultyLevelAndIsVisibleTrue(Dish.DifficultyLevel difficultyLevel);

    @Query("SELECT d FROM Dish d WHERE d.isVisible = true AND d.preparationTimeMinutes <= :maxTime")
    List<Dish> findByMaxPreparationTime(@Param("maxTime") Integer maxTime);

    @Query("SELECT d FROM Dish d WHERE d.isVerified = true AND d.isVisible = true")
    Page<Dish> findVerifiedDishes(Pageable pageable);

    long countByIsVisibleTrue();
}


