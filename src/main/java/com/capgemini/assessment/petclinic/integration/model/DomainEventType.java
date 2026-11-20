package com.capgemini.assessment.petclinic.integration.model;

/**
 * Enumerates relevant lifecycle state changes in PetClinic communicated to external systems.
 */
public enum DomainEventType {
    OWNER_CREATED,
    OWNER_UPDATED,
    OWNER_DELETED,
    PET_CREATED,
    PET_UPDATED,
    PET_DELETED,
    VISIT_CREATED,
    VISIT_UPDATED,
    VISIT_DELETED
}
