package com.capgemini.assessment.petclinic.integration.model;

/**
 * Identifies the PetClinic domain aggregate root that produced an integration change event.
 */
public enum AggregateType {
    OWNER,
    PET,
    VISIT
}
