package com.capgemini.assessment.petclinic.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Immutable response representation of a veterinary Visit.
 *
 * @param id unique visit identifier
 * @param visitDate date of visit
 * @param description clinical notes
 * @param vetName attending veterinarian
 * @param petId associated pet ID
 * @param petName associated pet name
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
@Schema(description = "Visit details returned by PetClinic REST API")
public record VisitResponseDto(
        @Schema(description = "Unique Visit ID", example = "1")
        Long id,
        @Schema(description = "Visit date", example = "2026-11-16")
        LocalDate visitDate,
        @Schema(description = "Visit description", example = "Annual health checkup and booster")
        String description,
        @Schema(description = "Attending veterinarian", example = "Dr. Helen Leary")
        String vetName,
        @Schema(description = "Pet ID", example = "1")
        Long petId,
        @Schema(description = "Pet name", example = "Simba")
        String petName,
        @Schema(description = "Creation UTC timestamp")
        Instant createdAt,
        @Schema(description = "Last update UTC timestamp")
        Instant updatedAt
) {
}
