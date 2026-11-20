package com.capgemini.assessment.petclinic.integration.dto;

/**
 * Value object encapsulating the outcome of an outbound transport delivery attempt.
 *
 * @param delivered true if the external system acknowledged the event with HTTP 2xx
 * @param httpStatus HTTP status code returned (or 0 on network/timeout exception)
 * @param diagnosticMessage human-readable response summary or error reason
 */
public record TransportDeliveryResult(
        boolean delivered,
        int httpStatus,
        String diagnosticMessage
) {

    /**
     * Factory method creating a successful delivery result.
     *
     * @param httpStatus HTTP 2xx status code
     * @param diagnosticMessage acknowledgement summary
     * @return successful TransportDeliveryResult
     */
    public static TransportDeliveryResult success(int httpStatus, String diagnosticMessage) {
        return new TransportDeliveryResult(true, httpStatus, diagnosticMessage);
    }

    /**
     * Factory method creating a failed delivery result.
     *
     * @param httpStatus HTTP error status code or 0 for network error
     * @param diagnosticMessage failure diagnostic description
     * @return failed TransportDeliveryResult
     */
    public static TransportDeliveryResult failure(int httpStatus, String diagnosticMessage) {
        return new TransportDeliveryResult(false, httpStatus, diagnosticMessage);
    }
}
