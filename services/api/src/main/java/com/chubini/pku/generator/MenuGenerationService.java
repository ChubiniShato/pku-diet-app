package com.chubini.pku.generator;

import com.chubini.pku.generator.dto.MenuGenerationRequest;
import com.chubini.pku.generator.dto.MenuGenerationResult;
import com.chubini.pku.menus.*;

import com.chubini.pku.norms.NormService;
import com.chubini.pku.norms.dto.NormPrescriptionDto;
import com.chubini.pku.patients.PatientService;
import com.chubini.pku.patients.dto.PatientProfileDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MenuGenerationService {

    private final PatientService patientService;
    private final NormService normService;
    private final MenuWeekRepository menuWeekRepository;
    private final MenuDayRepository menuDayRepository;

    /**
     * Generate a weekly menu using heuristic algorithm
     */
    @Transactional
    public MenuGenerationResult generateWeeklyMenu(MenuGenerationRequest request) {
        log.info("Generating weekly menu for patient: {} starting {}", request.patientId(), request.startDate());
        
        try {
            // Validate inputs
            PatientProfileDto patient = patientService.getPatientById(request.patientId());
            Optional<NormPrescriptionDto> currentNorm = normService.getCurrentNormForPatient(request.patientId());
            
            if (currentNorm.isEmpty()) {
                return MenuGenerationResult.failure("No active norm prescription found for patient");
            }
            
            // Create the menu week entity
            MenuWeek menuWeek = createMenuWeek(patient, request);
            
            // Generate daily menus for the week
            List<MenuDay> generatedDays = new ArrayList<>();
            LocalDate currentDate = request.startDate();
            
            for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                try {
                    MenuDay dayMenu = generateDailyMenu(menuWeek, currentDate, currentNorm.get(), request);
                    generatedDays.add(dayMenu);
                    currentDate = currentDate.plusDays(1);
                } catch (Exception e) {
                    log.error("Failed to generate menu for day {}: {}", currentDate, e.getMessage());
                    return MenuGenerationResult.failure("Failed to generate menu for " + currentDate + ": " + e.getMessage());
                }
            }
            
            // Save all generated data
            menuWeek.getMenuDays().addAll(generatedDays);
            MenuWeek savedWeek = menuWeekRepository.save(menuWeek);
            
            // Calculate totals
            calculateWeekTotals(savedWeek);
            
            log.info("Successfully generated weekly menu with ID: {}", savedWeek.getId());
            return MenuGenerationResult.success(savedWeek.getId(), "Weekly menu generated successfully");
            
        } catch (Exception e) {
            log.error("Failed to generate weekly menu: {}", e.getMessage(), e);
            return MenuGenerationResult.failure("Menu generation failed: " + e.getMessage());
        }
    }

    /**
     * Generate a single daily menu using heuristic algorithm
     */
    @Transactional
    public MenuGenerationResult generateDailyMenu(MenuGenerationRequest request) {
        log.info("Generating daily menu for patient: {} on {}", request.patientId(), request.startDate());
        
        try {
            // Validate inputs
            PatientProfileDto patient = patientService.getPatientById(request.patientId());
            Optional<NormPrescriptionDto> currentNorm = normService.getCurrentNormForPatient(request.patientId());
            
            if (currentNorm.isEmpty()) {
                return MenuGenerationResult.failure("No active norm prescription found for patient");
            }
            
            // Generate the daily menu
            MenuDay dayMenu = generateDailyMenu(null, request.startDate(), currentNorm.get(), request);
            MenuDay savedDay = menuDayRepository.save(dayMenu);
            
            log.info("Successfully generated daily menu with ID: {}", savedDay.getId());
            return MenuGenerationResult.success(savedDay.getId(), "Daily menu generated successfully");
            
        } catch (Exception e) {
            log.error("Failed to generate daily menu: {}", e.getMessage(), e);
            return MenuGenerationResult.failure("Menu generation failed: " + e.getMessage());
        }
    }

    /**
     * Create a MenuWeek entity for generation
     */
    private MenuWeek createMenuWeek(PatientProfileDto patient, MenuGenerationRequest request) {
        return MenuWeek.builder()
                .patient(patientService.getPatientEntity(patient.patientId()))
                .weekStartDate(request.startDate())
                .weekEndDate(request.startDate().plusDays(6))
                .generationMethod(MenuWeek.GenerationMethod.HEURISTIC)
                .status(MenuWeek.MenuStatus.GENERATED)
                .notes("Auto-generated using heuristic algorithm")
                .totalWeekPheMg(BigDecimal.ZERO)
                .totalWeekProteinG(BigDecimal.ZERO)
                .totalWeekKcal(BigDecimal.ZERO)
                .menuDays(new ArrayList<>())
                .build();
    }

    /**
     * Generate a single day's menu with balanced meals
     */
    private MenuDay generateDailyMenu(MenuWeek menuWeek, LocalDate date, NormPrescriptionDto norm, MenuGenerationRequest request) {
        log.debug("Generating daily menu for date: {}", date);
        
        // Create the day entity
        MenuDay menuDay = MenuDay.builder()
                .menuWeek(menuWeek)
                .patient(menuWeek != null ? menuWeek.getPatient() : patientService.getPatientEntity(request.patientId()))
                .date(date)
                .dayOfWeek(date.getDayOfWeek().getValue())
                .status(MenuWeek.MenuStatus.GENERATED)
                .notes("Auto-generated daily menu")
                .totalDayPheMg(BigDecimal.ZERO)
                .totalDayProteinG(BigDecimal.ZERO)
                .totalDayKcal(BigDecimal.ZERO)
                .totalDayFatG(BigDecimal.ZERO)
                .mealSlots(new ArrayList<>())
                .build();
        
        // Generate meal slots with basic distribution
        generateMealSlots(menuDay, norm, request);
        
        return menuDay;
    }

    /**
     * Generate meal slots for a day with nutritional distribution
     */
    private void generateMealSlots(MenuDay menuDay, NormPrescriptionDto norm, MenuGenerationRequest request) {
        // Define meal distribution percentages (PHE and calories)
        Map<MealSlot.SlotName, Double> pheDistribution = Map.of(
                MealSlot.SlotName.BREAKFAST, 0.25,
                MealSlot.SlotName.MORNING_SNACK, 0.10,
                MealSlot.SlotName.LUNCH, 0.30,
                MealSlot.SlotName.AFTERNOON_SNACK, 0.10,
                MealSlot.SlotName.DINNER, 0.20,
                MealSlot.SlotName.EVENING_SNACK, 0.05
        );
        
        Map<MealSlot.SlotName, Double> kcalDistribution = Map.of(
                MealSlot.SlotName.BREAKFAST, 0.25,
                MealSlot.SlotName.MORNING_SNACK, 0.10,
                MealSlot.SlotName.LUNCH, 0.35,
                MealSlot.SlotName.AFTERNOON_SNACK, 0.10,
                MealSlot.SlotName.DINNER, 0.15,
                MealSlot.SlotName.EVENING_SNACK, 0.05
        );
        
        // Create meal slots with target values
        for (MealSlot.SlotName slotName : MealSlot.SlotName.values()) {
            BigDecimal targetPhe = norm.dailyPheMgLimit() != null ? 
                    norm.dailyPheMgLimit().multiply(BigDecimal.valueOf(pheDistribution.get(slotName))) : 
                    BigDecimal.ZERO;
            
            BigDecimal targetKcal = norm.dailyKcalMin() != null ? 
                    norm.dailyKcalMin().multiply(BigDecimal.valueOf(kcalDistribution.get(slotName))) : 
                    BigDecimal.ZERO;
            
            MealSlot slot = MealSlot.builder()
                    .menuDay(menuDay)
                    .slotName(slotName)
                    .slotOrder(slotName.getDefaultOrder())
                    .targetPheMg(targetPhe)
                    .targetKcal(targetKcal)
                    .actualPheMg(BigDecimal.ZERO)
                    .actualProteinG(BigDecimal.ZERO)
                    .actualKcal(BigDecimal.ZERO)
                    .actualFatG(BigDecimal.ZERO)
                    .isConsumed(false)
                    .notes("Auto-generated meal slot")
                    .menuEntries(new ArrayList<>())
                    .build();
            
            // TODO: Add actual food items to the slot based on targets
            // This is where the real heuristic algorithm would select appropriate foods
            
            menuDay.getMealSlots().add(slot);
        }
    }

    /**
     * Calculate and update weekly totals
     */
    private void calculateWeekTotals(MenuWeek menuWeek) {
        BigDecimal totalPhe = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalKcal = BigDecimal.ZERO;
        
        for (MenuDay day : menuWeek.getMenuDays()) {
            if (day.getTotalDayPheMg() != null) {
                totalPhe = totalPhe.add(day.getTotalDayPheMg());
            }
            if (day.getTotalDayProteinG() != null) {
                totalProtein = totalProtein.add(day.getTotalDayProteinG());
            }
            if (day.getTotalDayKcal() != null) {
                totalKcal = totalKcal.add(day.getTotalDayKcal());
            }
        }
        
        menuWeek.setTotalWeekPheMg(totalPhe);
        menuWeek.setTotalWeekProteinG(totalProtein);
        menuWeek.setTotalWeekKcal(totalKcal);
    }
}
