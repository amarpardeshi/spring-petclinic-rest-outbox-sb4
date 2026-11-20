package com.capgemini.assessment.petclinic.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Validated request payload for creating or updating a PetClinic Owner.
 *
 * @param firstName given name of the owner
 * @param lastName family name of the owner
 * @param address street address of the owner
 * @param city city of residence
 * @param telephone numeric contact telephone (7 to 15 digits)
 */
@Schema(description = "Request payload for creating or updating an Owner")
public record OwnerRequestDto(
        @Schema(description = "Owner's first name", example = "Amar")
        @NotBlank(message = "First name is required")
        @Size(max = 80, message = "First name must not exceed 80 characters")
        String firstName,

        @Schema(description = "Owner's last name", example = "Pardeshi")
        @NotBlank(message = "Last name is required")
        @Size(max = 80, message = "Last name must not exceed 80 characters")
        String lastName,

        @Schema(description = "Street address", example = "A-403 Nivasa Elevia, Keshavnagar")
        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @Schema(description = "City name", example = "Pune")
        @NotBlank(message = "City is required")
        @Size(max = 80, message = "City must not exceed 80 characters")
        String city,

        @Schema(description = "Contact telephone number", example = "9876543210")
        @NotBlank(message = "Telephone is required")
        @Pattern(regexp = "^[0-9+\-]{7,20}$", message = "Telephone must be 7 to 20 digits")
        String telephone
) {
}
