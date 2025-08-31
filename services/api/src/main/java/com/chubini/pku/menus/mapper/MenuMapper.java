package com.chubini.pku.menus.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.chubini.pku.menus.*;
import com.chubini.pku.menus.dto.*;

import org.springframework.stereotype.Component;

@Component
public class MenuMapper {

  // MenuWeek mappings
  public MenuWeekDto toDto(MenuWeek entity) {
    if (entity == null) return null;

    return new MenuWeekDto(
        entity.getId(),
        entity.getPatient() != null ? entity.getPatient().getId() : null,
        entity.getWeekStartDate(),
        entity.getWeekEndDate(),
        entity.getGenerationMethod() != null ? entity.getGenerationMethod().name() : null,
        entity.getStatus() != null ? entity.getStatus().name() : null,
        null, // title - not in entity
        entity.getNotes(),
        entity.getMenuDays() != null ? toDayDto(entity.getMenuDays()) : null,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public List<MenuWeekDto> toWeekDto(List<MenuWeek> entities) {
    if (entities == null) return null;
    return entities.stream().map(this::toDto).collect(Collectors.toList());
  }

  public MenuWeek toEntity(CreateMenuWeekRequest request) {
    if (request == null) return null;

    return MenuWeek.builder()
        .weekStartDate(request.weekStartDate())
        .generationMethod(parseGenerationMethod(request.generationMethod()))
        .status(MenuWeek.MenuStatus.DRAFT)
        .notes(request.description())
        .build();
  }

  // MenuDay mappings
  public MenuDayDto toDto(MenuDay entity) {
    if (entity == null) return null;

    return new MenuDayDto(
        entity.getId(),
        entity.getMenuWeek() != null ? entity.getMenuWeek().getId() : null,
        entity.getDate(),
        dayOfWeekIntToString(entity.getDayOfWeek()),
        null, // title - not in entity
        entity.getNotes(),
        entity.getTotalDayPheMg(),
        entity.getTotalDayProteinG(),
        entity.getTotalDayKcal(),
        entity.getTotalDayFatG(),
        entity.getMealSlots() != null ? toSlotDto(entity.getMealSlots()) : null,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public List<MenuDayDto> toDayDto(List<MenuDay> entities) {
    if (entities == null) return null;
    return entities.stream().map(this::toDto).collect(Collectors.toList());
  }

  public MenuDay toEntity(CreateMenuDayRequest request) {
    if (request == null) return null;

    return MenuDay.builder()
        .date(request.menuDate())
        .dayOfWeek(request.menuDate() != null ? request.menuDate().getDayOfWeek().getValue() : null)
        .status(MenuWeek.MenuStatus.DRAFT)
        .notes(request.notes())
        .build();
  }

  // MealSlot mappings
  public MealSlotDto toDto(MealSlot entity) {
    if (entity == null) return null;

    return new MealSlotDto(
        entity.getId(),
        entity.getMenuDay() != null ? entity.getMenuDay().getId() : null,
        entity.getSlotName() != null ? entity.getSlotName().name() : null,
        null, // suggestedTime - not in entity
        entity.getSlotOrder(),
        entity.getNotes(),
        entity.getActualPheMg(),
        entity.getActualProteinG(),
        entity.getActualKcal(),
        entity.getActualFatG(),
        entity.getMenuEntries() != null ? toEntryDto(entity.getMenuEntries()) : null,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public List<MealSlotDto> toSlotDto(List<MealSlot> entities) {
    if (entities == null) return null;
    return entities.stream().map(this::toDto).collect(Collectors.toList());
  }

  // MenuEntry mappings
  public MenuEntryDto toDto(MenuEntry entity) {
    if (entity == null) return null;

    return new MenuEntryDto(
        entity.getId(),
        entity.getMealSlot() != null ? entity.getMealSlot().getId() : null,
        entity.getEntryType() != null ? entity.getEntryType().name() : null,
        null, // itemId - complex logic needed
        entity.getItemName(),
        entity.getPlannedServingGrams(),
        "grams",
        entity.getCalculatedPheMg(),
        entity.getCalculatedProteinG(),
        entity.getCalculatedKcal(),
        entity.getCalculatedFatG(),
        entity.getIsConsumed(),
        entity.getNotes(),
        null, // displayOrder - not in entity
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public List<MenuEntryDto> toEntryDto(List<MenuEntry> entities) {
    if (entities == null) return null;
    return entities.stream().map(this::toDto).collect(Collectors.toList());
  }

  public MenuEntry toEntity(AddMenuEntryRequest request) {
    if (request == null) return null;

    return MenuEntry.builder()
        .entryType(parseEntryType(request.entryType()))
        .plannedServingGrams(request.quantity())
        .notes(request.notes())
        .isConsumed(false)
        .build();
  }

  public void updateEntityFromRequest(UpdateMenuEntryRequest request, MenuEntry entity) {
    if (request == null || entity == null) return;

    if (request.quantity() != null) {
      entity.setPlannedServingGrams(request.quantity());
    }
    if (request.consumed() != null) {
      entity.setIsConsumed(request.consumed());
    }
    if (request.notes() != null) {
      entity.setNotes(request.notes());
    }
  }

  // Helper methods
  private String dayOfWeekIntToString(Integer dayOfWeek) {
    if (dayOfWeek == null) return null;
    return switch (dayOfWeek) {
      case 1 -> "MONDAY";
      case 2 -> "TUESDAY";
      case 3 -> "WEDNESDAY";
      case 4 -> "THURSDAY";
      case 5 -> "FRIDAY";
      case 6 -> "SATURDAY";
      case 7 -> "SUNDAY";
      default -> "UNKNOWN";
    };
  }

  private MenuWeek.GenerationMethod parseGenerationMethod(String method) {
    if (method == null || method.isBlank()) {
      return MenuWeek.GenerationMethod.MANUAL;
    }
    try {
      return MenuWeek.GenerationMethod.valueOf(method.toUpperCase());
    } catch (IllegalArgumentException e) {
      return MenuWeek.GenerationMethod.MANUAL;
    }
  }

  private MenuEntry.EntryType parseEntryType(String type) {
    if (type == null || type.isBlank()) {
      return MenuEntry.EntryType.PRODUCT;
    }
    try {
      return MenuEntry.EntryType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      return MenuEntry.EntryType.PRODUCT;
    }
  }
}
