package com.capgemini.assessment.petclinic.common.exception;

/**
 * Exception representing a failure during external integration event serialization or delivery.
 */
public class IntegrationDeliveryException extends RuntimeException {

    /**
     * Constructs an IntegrationDeliveryException with a descriptive message and root cause.
     *
     * @param message explanation of the integration failure
     * @param cause underlying exception cause
     */
    public IntegrationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an IntegrationDeliveryException with a descriptive message.
     *
     * @param message explanation of the integration failure
     */
    public IntegrationDeliveryException(String message) {
        super(message);
    }
}
