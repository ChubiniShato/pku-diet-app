package com.chubini.pku.menus;

import com.chubini.pku.menus.dto.*;
import com.chubini.pku.menus.mapper.MenuMapper;
import com.chubini.pku.patients.PatientService;
import com.chubini.pku.patients.PatientProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MenuService {

    private final MenuWeekRepository menuWeekRepository;
    private final MenuDayRepository menuDayRepository;
    private final MealSlotRepository mealSlotRepository;
    private final MenuEntryRepository menuEntryRepository;
    private final MenuMapper menuMapper;
    private final PatientService patientService;

    // ========== MenuWeek Operations ==========

    /**
     * Get all menu weeks for a patient
     */
    public List<MenuWeekDto> getMenuWeeksByPatient(UUID patientId) {
        log.debug("Getting menu weeks for patient: {}", patientId);
        List<MenuWeek> weeks = menuWeekRepository.findByPatientIdOrderByWeekStartDateDesc(patientId);
        return menuMapper.toWeekDto(weeks);
    }

    /**
     * Get menu week by ID
     */
    public MenuWeekDto getMenuWeekById(UUID weekId) {
        log.debug("Getting menu week by ID: {}", weekId);
        MenuWeek week = menuWeekRepository.findById(weekId)
                .orElseThrow(() -> new MenuNotFoundException("Menu week not found with ID: " + weekId));
        return menuMapper.toDto(week);
    }

    /**
     * Create a new menu week
     */
    @Transactional
    public MenuWeekDto createMenuWeek(CreateMenuWeekRequest request) {
        log.info("Creating new menu week for patient: {}", request.patientId());
        
        // Validate patient exists
        PatientProfile patient = patientService.getPatientEntity(request.patientId());
        
        // Convert request to entity
        MenuWeek week = menuMapper.toEntity(request);
        week.setPatient(patient);
        week.setWeekEndDate(request.weekStartDate().plusDays(6)); // Calculate end date
        
        // Save the week
        MenuWeek savedWeek = menuWeekRepository.save(week);
        
        log.info("Created menu week with ID: {}", savedWeek.getId());
        return menuMapper.toDto(savedWeek);
    }

    /**
     * Delete a menu week
     */
    @Transactional
    public void deleteMenuWeek(UUID weekId) {
        log.info("Deleting menu week: {}", weekId);
        
        if (!menuWeekRepository.existsById(weekId)) {
            throw new MenuNotFoundException("Menu week not found with ID: " + weekId);
        }
        
        menuWeekRepository.deleteById(weekId);
        log.info("Deleted menu week: {}", weekId);
    }

    // ========== MenuDay Operations ==========

    /**
     * Get menu days for a week
     */
    public List<MenuDayDto> getMenuDaysByWeek(UUID weekId) {
        log.debug("Getting menu days for week: {}", weekId);
        List<MenuDay> days = menuDayRepository.findByMenuWeekIdOrderByDate(weekId);
        return menuMapper.toDayDto(days);
    }

    /**
     * Get menu day by ID
     */
    public MenuDayDto getMenuDayById(UUID dayId) {
        log.debug("Getting menu day by ID: {}", dayId);
        MenuDay day = menuDayRepository.findById(dayId)
                .orElseThrow(() -> new MenuNotFoundException("Menu day not found with ID: " + dayId));
        return menuMapper.toDto(day);
    }

    /**
     * Create a new menu day
     */
    @Transactional
    public MenuDayDto createMenuDay(CreateMenuDayRequest request) {
        log.info("Creating new menu day for date: {}", request.menuDate());
        
        // Convert request to entity
        MenuDay day = menuMapper.toEntity(request);
        
        // Set week if provided
        if (request.weekId() != null) {
            MenuWeek week = menuWeekRepository.findById(request.weekId())
                    .orElseThrow(() -> new MenuNotFoundException("Menu week not found with ID: " + request.weekId()));
            day.setMenuWeek(week);
            day.setPatient(week.getPatient());
        }
        
        // Create default meal slots
        day = createDefaultMealSlots(day);
        
        // Save the day
        MenuDay savedDay = menuDayRepository.save(day);
        
        log.info("Created menu day with ID: {}", savedDay.getId());
        return menuMapper.toDto(savedDay);
    }

    /**
     * Delete a menu day
     */
    @Transactional
    public void deleteMenuDay(UUID dayId) {
        log.info("Deleting menu day: {}", dayId);
        
        if (!menuDayRepository.existsById(dayId)) {
            throw new MenuNotFoundException("Menu day not found with ID: " + dayId);
        }
        
        menuDayRepository.deleteById(dayId);
        log.info("Deleted menu day: {}", dayId);
    }

    // ========== MealSlot Operations ==========

    /**
     * Get meal slots for a day
     */
    public List<MealSlotDto> getMealSlotsByDay(UUID dayId) {
        log.debug("Getting meal slots for day: {}", dayId);
        List<MealSlot> slots = mealSlotRepository.findByMenuDayIdOrderBySlotOrder(dayId);
        return menuMapper.toSlotDto(slots);
    }

    // ========== MenuEntry Operations ==========

    /**
     * Get menu entries for a meal slot
     */
    public List<MenuEntryDto> getMenuEntriesBySlot(UUID slotId) {
        log.debug("Getting menu entries for slot: {}", slotId);
        List<MenuEntry> entries = menuEntryRepository.findByMealSlotIdOrderByCreatedAt(slotId);
        return menuMapper.toEntryDto(entries);
    }

    /**
     * Add a menu entry to a meal slot
     */
    @Transactional
    public MenuEntryDto addMenuEntry(UUID slotId, AddMenuEntryRequest request) {
        log.info("Adding menu entry to slot: {}", slotId);
        
        // Find the meal slot
        MealSlot slot = mealSlotRepository.findById(slotId)
                .orElseThrow(() -> new MenuNotFoundException("Meal slot not found with ID: " + slotId));
        
        // Convert request to entity
        MenuEntry entry = menuMapper.toEntity(request);
        entry.setMealSlot(slot);
        
        // TODO: Set the actual item references and calculate nutrition values
        // This will be implemented when we have the product/dish services integrated
        
        // Save the entry
        MenuEntry savedEntry = menuEntryRepository.save(entry);
        
        // Recalculate slot totals
        recalculateSlotTotals(slot);
        
        log.info("Added menu entry with ID: {}", savedEntry.getId());
        return menuMapper.toDto(savedEntry);
    }

    /**
     * Update a menu entry
     */
    @Transactional
    public MenuEntryDto updateMenuEntry(UUID entryId, UpdateMenuEntryRequest request) {
        log.info("Updating menu entry: {}", entryId);
        
        // Find existing entry
        MenuEntry existingEntry = menuEntryRepository.findById(entryId)
                .orElseThrow(() -> new MenuNotFoundException("Menu entry not found with ID: " + entryId));
        
        // Update the entry with new data
        menuMapper.updateEntityFromRequest(request, existingEntry);
        
        // TODO: Recalculate nutrition values if quantity changed
        
        // Save the updated entry
        MenuEntry updatedEntry = menuEntryRepository.save(existingEntry);
        
        // Recalculate slot totals
        recalculateSlotTotals(existingEntry.getMealSlot());
        
        log.info("Updated menu entry: {}", entryId);
        return menuMapper.toDto(updatedEntry);
    }

    /**
     * Delete a menu entry
     */
    @Transactional
    public void deleteMenuEntry(UUID entryId) {
        log.info("Deleting menu entry: {}", entryId);
        
        MenuEntry entry = menuEntryRepository.findById(entryId)
                .orElseThrow(() -> new MenuNotFoundException("Menu entry not found with ID: " + entryId));
        
        MealSlot slot = entry.getMealSlot();
        
        menuEntryRepository.deleteById(entryId);
        
        // Recalculate slot totals
        recalculateSlotTotals(slot);
        
        log.info("Deleted menu entry: {}", entryId);
    }

    /**
     * Mark menu entry as consumed
     */
    @Transactional
    public MenuEntryDto markEntryAsConsumed(UUID entryId, boolean consumed) {
        log.info("Marking menu entry {} as consumed: {}", entryId, consumed);
        
        MenuEntry entry = menuEntryRepository.findById(entryId)
                .orElseThrow(() -> new MenuNotFoundException("Menu entry not found with ID: " + entryId));
        
        entry.setIsConsumed(consumed);
        MenuEntry updatedEntry = menuEntryRepository.save(entry);
        
        return menuMapper.toDto(updatedEntry);
    }

    // ========== Helper Methods ==========

    /**
     * Create default meal slots for a menu day
     */
    private MenuDay createDefaultMealSlots(MenuDay day) {
        for (MealSlot.SlotName slotName : MealSlot.SlotName.values()) {
            MealSlot slot = MealSlot.builder()
                    .menuDay(day)
                    .slotName(slotName)
                    .slotOrder(slotName.getDefaultOrder())
                    .actualPheMg(BigDecimal.ZERO)
                    .actualProteinG(BigDecimal.ZERO)
                    .actualKcal(BigDecimal.ZERO)
                    .actualFatG(BigDecimal.ZERO)
                    .build();
            
            day.getMealSlots().add(slot);
        }
        return day;
    }

    /**
     * Recalculate nutrition totals for a meal slot
     */
    private void recalculateSlotTotals(MealSlot slot) {
        List<MenuEntry> entries = menuEntryRepository.findByMealSlotIdOrderByCreatedAt(slot.getId());
        
        BigDecimal totalPhe = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalKcal = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        
        for (MenuEntry entry : entries) {
            if (entry.getCalculatedPheMg() != null) {
                totalPhe = totalPhe.add(entry.getCalculatedPheMg());
            }
            if (entry.getCalculatedProteinG() != null) {
                totalProtein = totalProtein.add(entry.getCalculatedProteinG());
            }
            if (entry.getCalculatedKcal() != null) {
                totalKcal = totalKcal.add(entry.getCalculatedKcal());
            }
            if (entry.getCalculatedFatG() != null) {
                totalFat = totalFat.add(entry.getCalculatedFatG());
            }
        }
        
        slot.setActualPheMg(totalPhe);
        slot.setActualProteinG(totalProtein);
        slot.setActualKcal(totalKcal);
        slot.setActualFatG(totalFat);
        
        mealSlotRepository.save(slot);
        
        // Also update the day totals
        recalculateDayTotals(slot.getMenuDay());
    }

    /**
     * Recalculate nutrition totals for a menu day
     */
    private void recalculateDayTotals(MenuDay day) {
        List<MealSlot> slots = mealSlotRepository.findByMenuDayIdOrderBySlotOrder(day.getId());
        
        BigDecimal totalPhe = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalKcal = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        
        for (MealSlot slot : slots) {
            if (slot.getActualPheMg() != null) {
                totalPhe = totalPhe.add(slot.getActualPheMg());
            }
            if (slot.getActualProteinG() != null) {
                totalProtein = totalProtein.add(slot.getActualProteinG());
            }
            if (slot.getActualKcal() != null) {
                totalKcal = totalKcal.add(slot.getActualKcal());
            }
            if (slot.getActualFatG() != null) {
                totalFat = totalFat.add(slot.getActualFatG());
            }
        }
        
        day.setTotalDayPheMg(totalPhe);
        day.setTotalDayProteinG(totalProtein);
        day.setTotalDayKcal(totalKcal);
        day.setTotalDayFatG(totalFat);
        
        menuDayRepository.save(day);
    }
}
