package com.capgemini.assessment.petclinic.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Validated request payload for scheduling or updating a veterinary Visit.
 *
 * @param visitDate date of the visit
 * @param description clinical reason or notes for the visit
 * @param vetName attending veterinarian name
 * @param petId identifier of the Pet receiving care
 */
@Schema(description = "Request payload for scheduling or updating a Visit")
public record VisitRequestDto(
        @Schema(description = "Date of the visit", example = "2026-11-16")
        @NotNull(message = "Visit date is required")
        LocalDate visitDate,

        @Schema(description = "Clinical description or reason for visit", example = "Annual health checkup and booster")
        @NotBlank(message = "Visit description is required")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Schema(description = "Attending veterinarian name", example = "Dr. Helen Leary")
        @NotBlank(message = "Veterinarian name is required")
        @Size(max = 120, message = "Veterinarian name must not exceed 120 characters")
        String vetName,

        @Schema(description = "Pet ID", example = "1")
        @NotNull(message = "Pet ID is required")
        Long petId
) {
}
