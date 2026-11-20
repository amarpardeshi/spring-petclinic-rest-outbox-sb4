package com.capgemini.assessment.petclinic.integration.model;

/**
 * Configurable operational modes for the simulated external system webhook receiver.
 */
public enum ExternalSimulationMode {
    HEALTHY,
    TRANSIENT_FAILURE_503,
    SIMULATED_TIMEOUT
}
