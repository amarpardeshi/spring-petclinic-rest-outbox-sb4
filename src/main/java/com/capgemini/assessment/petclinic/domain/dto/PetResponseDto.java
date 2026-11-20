package com.capgemini.assessment.petclinic.domain.dto;

import com.capgemini.assessment.petclinic.domain.model.PetType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Immutable response representation of a Pet aggregate.
 *
 * @param id unique pet identifier
 * @param name pet name
 * @param birthDate pet birth date
 * @param petType species type
 * @param ownerId associated owner ID
 * @param ownerFullName associated owner's full name
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
@Schema(description = "Pet details returned by PetClinic REST API")
public record PetResponseDto(
        @Schema(description = "Unique Pet ID", example = "1")
        Long id,
        @Schema(description = "Pet name", example = "Simba")
        String name,
        @Schema(description = "Birth date", example = "2024-05-12")
        LocalDate birthDate,
        @Schema(description = "Pet species", example = "DOG")
        PetType petType,
        @Schema(description = "Owner ID", example = "1")
        Long ownerId,
        @Schema(description = "Owner full name", example = "Amar Pardeshi")
        String ownerFullName,
        @Schema(description = "Creation UTC timestamp")
        Instant createdAt,
        @Schema(description = "Last update UTC timestamp")
        Instant updatedAt
) {
}
