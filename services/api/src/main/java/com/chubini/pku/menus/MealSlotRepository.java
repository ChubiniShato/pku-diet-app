package com.chubini.pku.menus;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MealSlotRepository extends JpaRepository<MealSlot, UUID> {

  List<MealSlot> findByMenuDayIdOrderBySlotOrder(UUID menuDayId);

  List<MealSlot> findByMenuDayIdAndSlotName(UUID menuDayId, MealSlot.SlotName slotName);

  @Query(
      "SELECT ms FROM MealSlot ms WHERE ms.menuDay.patient.id = :patientId AND ms.isConsumed = :consumed")
  List<MealSlot> findByPatientIdAndConsumed(
      @Param("patientId") UUID patientId, @Param("consumed") Boolean consumed);

  @Query(
      "SELECT ms FROM MealSlot ms WHERE ms.menuDay.id = :menuDayId AND ms.actualPheMg > ms.targetPheMg")
  List<MealSlot> findByMenuDayIdAndPheExceeded(@Param("menuDayId") UUID menuDayId);

  long countByMenuDayIdAndIsConsumed(UUID menuDayId, Boolean isConsumed);
}
