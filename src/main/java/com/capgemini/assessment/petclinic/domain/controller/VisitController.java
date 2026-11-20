package com.capgemini.assessment.petclinic.domain.controller;

import com.capgemini.assessment.petclinic.common.constants.ApiConstants;
import com.capgemini.assessment.petclinic.common.dto.ApiResponseWrapper;
import com.capgemini.assessment.petclinic.domain.dto.VisitRequestDto;
import com.capgemini.assessment.petclinic.domain.dto.VisitResponseDto;
import com.capgemini.assessment.petclinic.domain.service.VisitService;
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
 * REST controller for PetClinic Visit operations with automatic Transactional Outbox event recording.
 */
@RestController
@RequestMapping(ApiConstants.VISITS_PATH)
@Tag(name = "03. Visits API", description = "Schedule and manage veterinary visits and record VISIT_* integration events")
public class VisitController {

    private final VisitService visitService;

    /**
     * Constructs VisitController with VisitService dependency.
     *
     * @param visitService business service for Visit aggregates
     */
    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    /**
     * Lists all visits or filters by petId.
     *
     * @param petId optional pet ID filter
     * @return HTTP 200 response with VisitResponseDto list
     */
    @GetMapping
    @Operation(summary = "List veterinary visits", description = "Returns all visits or filters by optional petId.")
    @ApiResponse(responseCode = "200", description = "Visits retrieved successfully")
    public ResponseEntity<ApiResponseWrapper<List<VisitResponseDto>>> listVisits(
            @Parameter(description = "Optional pet ID filter")
            @RequestParam(name = "petId", required = false) Long petId) {
        List<VisitResponseDto> visits = visitService.findVisits(petId);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Visits fetched successfully", visits));
    }

    /**
     * Retrieves a single visit by ID.
     *
     * @param visitId unique visit ID
     * @return HTTP 200 response with VisitResponseDto
     */
    @GetMapping("/{visitId}")
    @Operation(summary = "Get visit by ID", description = "Fetches a single veterinary visit by primary key ID.")
    @ApiResponse(responseCode = "200", description = "Visit found")
    @ApiResponse(responseCode = "404", description = "Visit not found")
    public ResponseEntity<ApiResponseWrapper<VisitResponseDto>> getVisitById(
            @PathVariable("visitId") Long visitId) {
        VisitResponseDto visit = visitService.findVisitById(visitId);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Visit fetched successfully", visit));
    }

    /**
     * Schedules a new visit and records a VISIT_CREATED outbox event.
     *
     * @param request validated VisitRequestDto payload
     * @return HTTP 201 response with created VisitResponseDto
     */
    @PostMapping
    @Operation(summary = "Schedule a new visit", description = "Creates a Visit and writes a VISIT_CREATED event to the transactional outbox.")
    @ApiResponse(responseCode = "201", description = "Visit scheduled and outbox event recorded")
    public ResponseEntity<ApiResponseWrapper<VisitResponseDto>> createVisit(
            @Valid @RequestBody VisitRequestDto request) {
        VisitResponseDto created = visitService.createVisit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.ok("Visit scheduled and queued for external sync", created));
    }

    /**
     * Updates an existing visit and records a VISIT_UPDATED outbox event.
     *
     * @param visitId unique visit ID to update
     * @param request validated VisitRequestDto payload
     * @return HTTP 200 response with updated VisitResponseDto
     */
    @PutMapping("/{visitId}")
    @Operation(summary = "Update a visit", description = "Updates a Visit and writes a VISIT_UPDATED event to the transactional outbox.")
    @ApiResponse(responseCode = "200", description = "Visit updated and outbox event recorded")
    public ResponseEntity<ApiResponseWrapper<VisitResponseDto>> updateVisit(
            @PathVariable("visitId") Long visitId,
            @Valid @RequestBody VisitRequestDto request) {
        VisitResponseDto updated = visitService.updateVisit(visitId, request);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Visit updated and queued for external sync", updated));
    }

    /**
     * Deletes a visit by ID and records a VISIT_DELETED outbox event.
     *
     * @param visitId unique visit ID to delete
     * @return HTTP 200 confirmation response
     */
    @DeleteMapping("/{visitId}")
    @Operation(summary = "Delete a visit", description = "Deletes a Visit and writes a VISIT_DELETED event to the transactional outbox.")
    @ApiResponse(responseCode = "200", description = "Visit deleted and outbox event recorded")
    public ResponseEntity<ApiResponseWrapper<Void>> deleteVisit(
            @PathVariable("visitId") Long visitId) {
        visitService.deleteVisit(visitId);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Visit deleted and queued for external sync", null));
    }
}
