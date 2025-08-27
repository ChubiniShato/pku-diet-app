package com.chubini.pku.dishes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DishItemRepository extends JpaRepository<DishItem, UUID> {

    /**
     * Find all items for a specific dish
     */
    List<DishItem> findByDishId(UUID dishId);

    /**
     * Find items by dish ID with product information (fetch join)
     */
    @Query("SELECT di FROM DishItem di JOIN FETCH di.product WHERE di.dish.id = :dishId")
    List<DishItem> findByDishIdWithProduct(@Param("dishId") UUID dishId);

    /**
     * Delete all items for a specific dish
     */
    @Modifying
    @Query("DELETE FROM DishItem di WHERE di.dish.id = :dishId")
    void deleteByDishId(@Param("dishId") UUID dishId);

    /**
     * Find items that use a specific product
     */
    @Query("SELECT di FROM DishItem di WHERE di.product.id = :productId")
    List<DishItem> findByProductId(@Param("productId") UUID productId);

    /**
     * Count items in a dish
     */
    @Query("SELECT COUNT(di) FROM DishItem di WHERE di.dish.id = :dishId")
    long countByDishId(@Param("dishId") UUID dishId);

    /**
     * Check if a dish contains a specific product
     */
    @Query("SELECT CASE WHEN COUNT(di) > 0 THEN true ELSE false END FROM DishItem di WHERE di.dish.id = :dishId AND di.product.id = :productId")
    boolean existsByDishIdAndProductId(@Param("dishId") UUID dishId, @Param("productId") UUID productId);
}
