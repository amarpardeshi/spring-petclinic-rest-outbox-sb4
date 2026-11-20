package com.capgemini.assessment.petclinic.domain.controller;

import com.capgemini.assessment.petclinic.common.constants.ApiConstants;
import com.capgemini.assessment.petclinic.common.dto.ApiResponseWrapper;
import com.capgemini.assessment.petclinic.domain.dto.OwnerRequestDto;
import com.capgemini.assessment.petclinic.domain.dto.OwnerResponseDto;
import com.capgemini.assessment.petclinic.domain.service.OwnerService;
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
 * REST controller for PetClinic Owner operations with automatic Transactional Outbox event recording.
 */
@RestController
@RequestMapping(ApiConstants.OWNERS_PATH)
@Tag(name = "01. Owners API", description = "Manage PetClinic owners and record OWNER_* integration events")
public class OwnerController {

    private final OwnerService ownerService;

    /**
     * Constructs OwnerController with OwnerService dependency.
     *
     * @param ownerService business service for Owner aggregates
     */
    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    /**
     * Lists all owners or filters by lastName prefix.
     *
     * @param lastName optional last name prefix filter
     * @return HTTP 200 response with matching OwnerResponseDto list
     */
    @GetMapping
    @Operation(summary = "List pet owners", description = "Returns all owners or filters by optional lastName prefix.")
    @ApiResponse(responseCode = "200", description = "Owners retrieved successfully")
    public ResponseEntity<ApiResponseWrapper<List<OwnerResponseDto>>> listOwners(
            @Parameter(description = "Optional last name prefix filter")
            @RequestParam(name = "lastName", required = false) String lastName) {
        List<OwnerResponseDto> owners = ownerService.findOwners(lastName);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Owners fetched successfully", owners));
    }

    /**
     * Retrieves a single owner by ID.
     *
     * @param ownerId unique owner ID
     * @return HTTP 200 response with OwnerResponseDto
     */
    @GetMapping("/{ownerId}")
    @Operation(summary = "Get owner by ID", description = "Fetches a single PetClinic owner by primary key ID.")
    @ApiResponse(responseCode = "200", description = "Owner found")
    @ApiResponse(responseCode = "404", description = "Owner not found")
    public ResponseEntity<ApiResponseWrapper<OwnerResponseDto>> getOwnerById(
            @PathVariable("ownerId") Long ownerId) {
        OwnerResponseDto owner = ownerService.findOwnerById(ownerId);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Owner fetched successfully", owner));
    }

    /**
     * Creates a new owner and records an OWNER_CREATED outbox event.
     *
     * @param request validated OwnerRequestDto payload
     * @return HTTP 201 response with created OwnerResponseDto
     */
    @PostMapping
    @Operation(summary = "Create a new owner", description = "Creates a PetClinic owner and writes an OWNER_CREATED event to the transactional outbox.")
    @ApiResponse(responseCode = "201", description = "Owner created and outbox event recorded")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<ApiResponseWrapper<OwnerResponseDto>> createOwner(
            @Valid @RequestBody OwnerRequestDto request) {
        OwnerResponseDto created = ownerService.createOwner(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.ok("Owner created and queued for external sync", created));
    }

    /**
     * Updates an existing owner and records an OWNER_UPDATED outbox event.
     *
     * @param ownerId unique owner ID to update
     * @param request validated OwnerRequestDto payload
     * @return HTTP 200 response with updated OwnerResponseDto
     */
    @PutMapping("/{ownerId}")
    @Operation(summary = "Update an owner", description = "Updates an existing owner and writes an OWNER_UPDATED event to the transactional outbox.")
    @ApiResponse(responseCode = "200", description = "Owner updated and outbox event recorded")
    @ApiResponse(responseCode = "404", description = "Owner not found")
    public ResponseEntity<ApiResponseWrapper<OwnerResponseDto>> updateOwner(
            @PathVariable("ownerId") Long ownerId,
            @Valid @RequestBody OwnerRequestDto request) {
        OwnerResponseDto updated = ownerService.updateOwner(ownerId, request);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Owner updated and queued for external sync", updated));
    }

    /**
     * Deletes an owner by ID and records an OWNER_DELETED outbox event.
     *
     * @param ownerId unique owner ID to delete
     * @return HTTP 200 confirmation response
     */
    @DeleteMapping("/{ownerId}")
    @Operation(summary = "Delete an owner", description = "Deletes an owner and writes an OWNER_DELETED event to the transactional outbox.")
    @ApiResponse(responseCode = "200", description = "Owner deleted and outbox event recorded")
    @ApiResponse(responseCode = "404", description = "Owner not found")
    public ResponseEntity<ApiResponseWrapper<Void>> deleteOwner(
            @PathVariable("ownerId") Long ownerId) {
        ownerService.deleteOwner(ownerId);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Owner deleted and queued for external sync", null));
    }
}
