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
@Table(name = "menu_week")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private MenuStatus status = MenuStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_method")
    private GenerationMethod generationMethod;

    @Column(name = "total_week_phe_mg", precision = 10, scale = 2)
    private BigDecimal totalWeekPheMg;

    @Column(name = "total_week_protein_g", precision = 10, scale = 2)
    private BigDecimal totalWeekProteinG;

    @Column(name = "total_week_kcal", precision = 10, scale = 2)
    private BigDecimal totalWeekKcal;

    @Column(name = "notes")
    private String notes;

    @OneToMany(mappedBy = "menuWeek", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuDay> menuDays = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum MenuStatus {
        DRAFT, GENERATED, APPROVED, CONSUMED, ARCHIVED
    }

    public enum GenerationMethod {
        MANUAL, HEURISTIC, OPTIMIZED
    }
}


