package com.capgemini.assessment.petclinic.domain.controller;

import com.capgemini.assessment.petclinic.common.constants.ApiConstants;
import com.capgemini.assessment.petclinic.common.dto.ApiResponseWrapper;
import com.capgemini.assessment.petclinic.domain.dto.PetRequestDto;
import com.capgemini.assessment.petclinic.domain.dto.PetResponseDto;
import com.capgemini.assessment.petclinic.domain.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for PetClinic Pet operations with automatic Transactional Outbox event recording.
 */
@RestController
@RequestMapping(ApiConstants.PETS_PATH)
@Tag(name = "02. Pets API", description = "Manage PetClinic pets and record PET_* integration events")
public class PetController {

    private final PetService petService;

    /**
     * Constructs PetController with PetService dependency.
     *
     * @param petService business service for Pet aggregates
     */
    public PetController(PetService petService) {
        this.petService = petService;
    }

    /**
     * Lists all pets or filters by ownerId.
     *
     * @param ownerId optional owner ID filter
     * @return HTTP 200 response with PetResponseDto list
     */
    @GetMapping
    @Operation(summary = "List pets", description = "Returns all pets or filters by ownerId.")
    @ApiResponse(responseCode = "200", description = "Pets retrieved successfully")
    public ResponseEntity<ApiResponseWrapper<List<PetResponseDto>>> listPets(
            @Parameter(description = "Optional owner ID filter")
            @RequestParam(name = "ownerId", required = false) Long ownerId) {
        List<PetResponseDto> pets = petService.findPets(ownerId);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Pets fetched successfully", pets));
    }

    /**
     * Retrieves a single pet by ID.
     *
     * @param petId unique pet ID
     * @return HTTP 200 response with PetResponseDto
     */
    @GetMapping("/{petId}")
    @Operation(summary = "Get pet by ID", description = "Fetches a single Pet by primary key ID.")
    @ApiResponse(responseCode = "200", description = "Pet found")
    @ApiResponse(responseCode = "404", description = "Pet not found")
    public ResponseEntity<ApiResponseWrapper<PetResponseDto>> getPetById(
            @PathVariable("petId") Long petId) {
        PetResponseDto pet = petService.findPetById(petId);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Pet fetched successfully", pet));
    }

    /**
     * Registers a new pet and records a PET_CREATED outbox event.
     *
     * @param request validated PetRequestDto payload
     * @return HTTP 201 response with created PetResponseDto
     */
    @PostMapping
    @Operation(summary = "Register a new pet", description = "Creates a Pet under an Owner and writes a PET_CREATED event to the transactional outbox.")
    @ApiResponse(responseCode = "201", description = "Pet created and outbox event recorded")
    public ResponseEntity<ApiResponseWrapper<PetResponseDto>> createPet(
            @Valid @RequestBody PetRequestDto request) {
        PetResponseDto created = petService.createPet(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.ok("Pet created and queued for external sync", created));
    }

    /**
     * Updates an existing pet and records a PET_UPDATED outbox event.
     *
     * @param petId unique pet ID to update
     * @param request validated PetRequestDto payload
     * @return HTTP 200 response with updated PetResponseDto
     */
    @PutMapping("/{petId}")
    @Operation(summary = "Update a pet", description = "Updates a Pet and writes a PET_UPDATED event to the transactional outbox.")
    @ApiResponse(responseCode = "200", description = "Pet updated and outbox event recorded")
    public ResponseEntity<ApiResponseWrapper<PetResponseDto>> updatePet(
            @PathVariable("petId") Long petId,
            @Valid @RequestBody PetRequestDto request) {
        PetResponseDto updated = petService.updatePet(petId, request);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Pet updated and queued for external sync", updated));
    }

    /**
     * Deletes a pet by ID and records a PET_DELETED outbox event.
     *
     * @param petId unique pet ID to delete
     * @return HTTP 200 confirmation response
     */
    @DeleteMapping("/{petId}")
    @Operation(summary = "Delete a pet", description = "Deletes a Pet and writes a PET_DELETED event to the transactional outbox.")
    @ApiResponse(responseCode = "200", description = "Pet deleted and outbox event recorded")
    public ResponseEntity<ApiResponseWrapper<Void>> deletePet(
            @PathVariable("petId") Long petId) {
        petService.deletePet(petId);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Pet deleted and queued for external sync", null));
    }
}
