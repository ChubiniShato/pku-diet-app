package com.chubini.pku.menus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MenuEntryRepository extends JpaRepository<MenuEntry, UUID> {

    List<MenuEntry> findByMealSlotId(UUID mealSlotId);

    List<MenuEntry> findByMealSlotIdAndIsAlternative(UUID mealSlotId, Boolean isAlternative);

    @Query("SELECT me FROM MenuEntry me WHERE me.mealSlot.menuDay.patient.id = :patientId AND me.entryType = :entryType")
    List<MenuEntry> findByPatientIdAndEntryType(@Param("patientId") UUID patientId, @Param("entryType") MenuEntry.EntryType entryType);

    @Query("SELECT me FROM MenuEntry me WHERE me.product.id = :productId")
    List<MenuEntry> findByProductId(@Param("productId") UUID productId);

    @Query("SELECT me FROM MenuEntry me WHERE me.customProduct.id = :customProductId")
    List<MenuEntry> findByCustomProductId(@Param("customProductId") UUID customProductId);

    @Query("SELECT me FROM MenuEntry me WHERE me.dish.id = :dishId")
    List<MenuEntry> findByDishId(@Param("dishId") UUID dishId);

    @Query("SELECT me FROM MenuEntry me WHERE me.customDish.id = :customDishId")
    List<MenuEntry> findByCustomDishId(@Param("customDishId") UUID customDishId);

    long countByMealSlotIdAndIsConsumed(UUID mealSlotId, Boolean isConsumed);

    List<MenuEntry> findByMealSlotIdOrderByCreatedAt(UUID mealSlotId);
}

