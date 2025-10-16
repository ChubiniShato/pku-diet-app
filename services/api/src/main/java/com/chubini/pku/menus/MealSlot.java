package com.chubini.pku.menus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meal_slot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealSlot {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "menu_day_id", nullable = false)
  private MenuDay menuDay;

  @Enumerated(EnumType.STRING)
  @Column(name = "slot_name", nullable = false)
  private SlotName slotName;

  @Column(name = "slot_order", nullable = false)
  private Integer slotOrder;

  @Column(name = "target_phe_mg", precision = 8, scale = 2)
  private BigDecimal targetPheMg;

  @Column(name = "target_kcal", precision = 8, scale = 2)
  private BigDecimal targetKcal;

  @Column(name = "actual_phe_mg", precision = 8, scale = 2)
  private BigDecimal actualPheMg;

  @Column(name = "actual_protein_g", precision = 8, scale = 2)
  private BigDecimal actualProteinG;

  @Column(name = "actual_kcal", precision = 8, scale = 2)
  private BigDecimal actualKcal;

  @Column(name = "actual_fat_g", precision = 8, scale = 2)
  private BigDecimal actualFatG;

  @Column(name = "is_consumed", nullable = false)
  @Builder.Default
  private Boolean isConsumed = false;

  @Column(name = "consumed_at")
  private LocalDateTime consumedAt;

  @Column(name = "notes")
  private String notes;

  @OneToMany(mappedBy = "mealSlot", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<MenuEntry> menuEntries = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (slotOrder == null && slotName != null) {
      slotOrder = slotName.getDefaultOrder();
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public enum SlotName {
    BREAKFAST(1),
    MORNING_SNACK(2),
    LUNCH(3),
    AFTERNOON_SNACK(4),
    DINNER(5),
    EVENING_SNACK(6);

    private final int defaultOrder;

    SlotName(int defaultOrder) {
      this.defaultOrder = defaultOrder;
    }

    public int getDefaultOrder() {
      return defaultOrder;
    }
  }
}
