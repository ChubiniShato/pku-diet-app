package com.chubini.pku.norms.mapper;

import com.chubini.pku.norms.NormPrescription;
import com.chubini.pku.norms.dto.CreateNormPrescriptionRequest;
import com.chubini.pku.norms.dto.NormPrescriptionDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NormPrescriptionMapper {

    public NormPrescriptionDto toDto(NormPrescription entity) {
        if (entity == null) return null;
        
        return new NormPrescriptionDto(
                entity.getId(),
                entity.getPatient() != null ? entity.getPatient().getId() : null,
                entity.getPheLimitMgPerDay(),
                entity.getProteinLimitGPerDay(),
                entity.getKcalMinPerDay(),
                null, // dailyKcalMax - not in entity
                entity.getFatLimitGPerDay(),
                entity.getPrescribedDate(),
                null, // effectiveUntil - not in entity
                null, // prescribedBy - not in entity
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<NormPrescriptionDto> toDto(List<NormPrescription> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    public NormPrescription toEntity(CreateNormPrescriptionRequest request) {
        if (request == null) return null;
        
        return NormPrescription.builder()
                .pheLimitMgPerDay(request.dailyPheMgLimit())
                .proteinLimitGPerDay(request.dailyProteinGLimit())
                .kcalMinPerDay(request.dailyKcalMin())
                .fatLimitGPerDay(request.dailyFatGMax())
                .prescribedDate(request.effectiveFrom())
                .notes(request.clinicalNotes())
                .isActive(true)
                .build();
    }
}