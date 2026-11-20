package com.capgemini.assessment.petclinic.domain.dto;

import com.capgemini.assessment.petclinic.domain.model.PetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Validated request payload for registering or updating a Pet in PetClinic.
 *
 * @param name pet's name
 * @param birthDate pet's birth date
 * @param petType species classification
 * @param ownerId identifier of the owning Owner
 */
@Schema(description = "Request payload for creating or updating a Pet")
public record PetRequestDto(
        @Schema(description = "Pet's name", example = "Simba")
        @NotBlank(message = "Pet name is required")
        @Size(max = 80, message = "Pet name must not exceed 80 characters")
        String name,

        @Schema(description = "Pet's date of birth", example = "2024-05-12")
        @NotNull(message = "Birth date is required")
        @PastOrPresent(message = "Birth date cannot be in the future")
        LocalDate birthDate,

        @Schema(description = "Species type", example = "DOG")
        @NotNull(message = "Pet type is required")
        PetType petType,

        @Schema(description = "Owner ID", example = "1")
        @NotNull(message = "Owner ID is required")
        Long ownerId
) {
}
