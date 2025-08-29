package com.chubini.pku.patients.mapper;

import com.chubini.pku.patients.PatientProfile;
import com.chubini.pku.patients.dto.CreatePatientProfileRequest;
import com.chubini.pku.patients.dto.PatientProfileDto;
import com.chubini.pku.patients.dto.UpdatePatientProfileRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PatientProfileMapper {

    public PatientProfileDto toDto(PatientProfile entity) {
        if (entity == null) return null;
        
        return new PatientProfileDto(
                entity.getId(),
                entity.getName(),
                null, // email - not in entity yet
                entity.getBirthDate(),
                null, // gender - not in entity yet
                entity.getWeightKg(),
                entity.getHeightCm(),
                entity.getActivityLevel() != null ? entity.getActivityLevel().name() : null,
                null, // dietaryPreferences - not in entity yet
                null, // medicalNotes - not in entity yet
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<PatientProfileDto> toDto(List<PatientProfile> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    public PatientProfile toEntity(CreatePatientProfileRequest request) {
        if (request == null) return null;
        
        return PatientProfile.builder()
                .name(request.name())
                .birthDate(request.dateOfBirth())
                .weightKg(request.weightKg())
                .heightCm(request.heightCm())
                .activityLevel(parseActivityLevel(request.activityLevel()))
                .build();
    }

    public void updateEntityFromRequest(UpdatePatientProfileRequest request, PatientProfile entity) {
        if (request == null || entity == null) return;
        
        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (request.dateOfBirth() != null) {
            entity.setBirthDate(request.dateOfBirth());
        }
        if (request.weightKg() != null) {
            entity.setWeightKg(request.weightKg());
        }
        if (request.heightCm() != null) {
            entity.setHeightCm(request.heightCm());
        }
        if (request.activityLevel() != null) {
            entity.setActivityLevel(parseActivityLevel(request.activityLevel()));
        }
    }

    private PatientProfile.ActivityLevel parseActivityLevel(String activityLevel) {
        if (activityLevel == null || activityLevel.isBlank()) {
            return PatientProfile.ActivityLevel.LOW;
        }
        try {
            return PatientProfile.ActivityLevel.valueOf(activityLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PatientProfile.ActivityLevel.LOW;
        }
    }
}