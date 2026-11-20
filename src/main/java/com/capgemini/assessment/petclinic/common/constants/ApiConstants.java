package com.capgemini.assessment.petclinic.common.constants;

/**
 * Central API path, header, and schema constants shared across controllers and integration adapters.
 */
public final class ApiConstants {

    public static final String API_V1_BASE = "/api/v1";
    public static final String OWNERS_PATH = API_V1_BASE + "/owners";
    public static final String PETS_PATH = API_V1_BASE + "/pets";
    public static final String VISITS_PATH = API_V1_BASE + "/visits";
    public static final String OUTBOX_PATH = API_V1_BASE + "/integration/outbox";
    public static final String MOCK_EXTERNAL_PATH = API_V1_BASE + "/mock-external";

    public static final String HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key";
    public static final String HEADER_EVENT_TYPE = "X-Event-Type";
    public static final String HEADER_PAYLOAD_HASH = "X-Payload-SHA256";
    public static final String SPEC_VERSION = "1.0";

    /**
     * Private constructor to prevent instantiation of utility constant class.
     */
    private ApiConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
