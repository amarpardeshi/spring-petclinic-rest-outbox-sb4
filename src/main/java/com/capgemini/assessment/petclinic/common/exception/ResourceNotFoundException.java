package com.capgemini.assessment.petclinic.common.exception;

/**
 * Domain exception thrown when a requested PetClinic entity or Outbox event does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a ResourceNotFoundException with an explicit resource name and identifier.
     *
     * @param resourceName name of the domain aggregate or resource
     * @param identifier unique identifier that was not found
     */
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(resourceName + " not found with identifier: " + identifier);
    }
}
