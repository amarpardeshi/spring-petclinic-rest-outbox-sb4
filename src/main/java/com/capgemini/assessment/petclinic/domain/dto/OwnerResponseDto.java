package com.capgemini.assessment.petclinic.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Immutable response representation of a PetClinic Owner aggregate.
 *
 * @param id unique owner identifier
 * @param firstName owner's first name
 * @param lastName owner's last name
 * @param address street address
 * @param city city name
 * @param telephone contact telephone number
 * @param createdAt creation timestamp
 * @param updatedAt last modification timestamp
 */
@Schema(description = "Owner details returned by PetClinic REST API")
public record OwnerResponseDto(
        @Schema(description = "Unique Owner ID", example = "1")
        Long id,
        @Schema(description = "First name", example = "Amar")
        String firstName,
        @Schema(description = "Last name", example = "Pardeshi")
        String lastName,
        @Schema(description = "Street address", example = "A-403 Nivasa Elevia, Keshavnagar")
        String address,
        @Schema(description = "City", example = "Pune")
        String city,
        @Schema(description = "Telephone", example = "9876543210")
        String telephone,
        @Schema(description = "Creation UTC timestamp")
        Instant createdAt,
        @Schema(description = "Last update UTC timestamp")
        Instant updatedAt
) {
}
