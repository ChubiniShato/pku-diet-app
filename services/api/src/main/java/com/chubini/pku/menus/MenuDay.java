package com.chubini.pku.menus;

import com.chubini.pku.patients.PatientProfile;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "menu_day")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuDay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_week_id")
    private MenuWeek menuWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private MenuWeek.MenuStatus status = MenuWeek.MenuStatus.DRAFT;

    @Column(name = "total_day_phe_mg", precision = 8, scale = 2)
    private BigDecimal totalDayPheMg;

    @Column(name = "total_day_protein_g", precision = 8, scale = 2)
    private BigDecimal totalDayProteinG;

    @Column(name = "total_day_kcal", precision = 8, scale = 2)
    private BigDecimal totalDayKcal;

    @Column(name = "total_day_fat_g", precision = 8, scale = 2)
    private BigDecimal totalDayFatG;

    @Column(name = "notes")
    private String notes;

    @OneToMany(mappedBy = "menuDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MealSlot> mealSlots = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (dayOfWeek == null && date != null) {
            dayOfWeek = date.getDayOfWeek().getValue();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


