package com.capgemini.assessment.petclinic.domain.service;

import com.capgemini.assessment.petclinic.domain.dto.VisitRequestDto;
import com.capgemini.assessment.petclinic.domain.dto.VisitResponseDto;
import java.util.List;

/**
 * Business service contract for managing veterinary Visit aggregates and recording visit lifecycle events.
 */
public interface VisitService {

    /**
     * Retrieves all visits or filters by petId when provided.
     *
     * @param petId optional pet ID filter
     * @return list of VisitResponseDto objects
     */
    List<VisitResponseDto> findVisits(Long petId);

    /**
     * Retrieves a single Visit by primary key ID.
     *
     * @param visitId unique visit ID
     * @return VisitResponseDto representation
     */
    VisitResponseDto findVisitById(Long visitId);

    /**
     * Schedules a new Visit and atomically records a VISIT_CREATED outbox event.
     *
     * @param request validated VisitRequestDto
     * @return created VisitResponseDto
     */
    VisitResponseDto createVisit(VisitRequestDto request);

    /**
     * Updates an existing Visit and atomically records a VISIT_UPDATED outbox event.
     *
     * @param visitId unique visit ID to update
     * @param request validated VisitRequestDto
     * @return updated VisitResponseDto
     */
    VisitResponseDto updateVisit(Long visitId, VisitRequestDto request);

    /**
     * Deletes a Visit by ID and atomically records a VISIT_DELETED outbox event.
     *
     * @param visitId unique visit ID to delete
     */
    void deleteVisit(Long visitId);
}
