package com.capgemini.assessment.petclinic.integration.controller;

import com.capgemini.assessment.petclinic.common.constants.ApiConstants;
import com.capgemini.assessment.petclinic.common.dto.ApiResponseWrapper;
import com.capgemini.assessment.petclinic.integration.dto.IntegrationEventEnvelope;
import com.capgemini.assessment.petclinic.integration.dto.WebhookReceiptDto;
import com.capgemini.assessment.petclinic.integration.model.ExternalSimulationMode;
import com.capgemini.assessment.petclinic.integration.model.ProcessedWebhookEvent;
import com.capgemini.assessment.petclinic.integration.service.IdempotentWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulated External Consumer System controller demonstrating idempotent webhook reception and failure injection.
 */
@RestController
@RequestMapping(ApiConstants.MOCK_EXTERNAL_PATH)
@Tag(name = "05. Simulated External Consumer & Chaos API", description = "Idempotent webhook receiver and failure-mode toggle for verifying retries and recovery")
public class ExternalSystemSimulationController {

    private final IdempotentWebhookService idempotentWebhookService;

    /**
     * Constructs ExternalSystemSimulationController with IdempotentWebhookService dependency.
     *
     * @param idempotentWebhookService service handling inbox deduplication and simulation state
     */
    public ExternalSystemSimulationController(IdempotentWebhookService idempotentWebhookService) {
        this.idempotentWebhookService = idempotentWebhookService;
    }

    /**
     * Receives outbound webhook notifications from PetClinic and enforces idempotency via X-Idempotency-Key.
     *
     * @param envelope CloudEvents-aligned payload envelope
     * @param idempotencyKey optional X-Idempotency-Key HTTP header
     * @return HTTP 200 receipt when HEALTHY, or HTTP 503/504 when failure simulation is active
     */
    @PostMapping("/webhook")
    @Operation(summary = "Receive PetClinic webhook event", description = "Processes incoming PetClinic events idempotently or simulates transient 503/timeout failures.")
    @ApiResponse(responseCode = "200", description = "Event processed or duplicate suppressed")
    @ApiResponse(responseCode = "503", description = "Simulated external system unavailability")
    public ResponseEntity<ApiResponseWrapper<WebhookReceiptDto>> receiveWebhook(
            @RequestBody IntegrationEventEnvelope envelope,
            @RequestHeader(name = ApiConstants.HEADER_IDEMPOTENCY_KEY, required = false) String idempotencyKey) {
        ExternalSimulationMode mode = idempotentWebhookService.getSimulationMode();
        if (mode == ExternalSimulationMode.TRANSIENT_FAILURE_503) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponseWrapper.error("Simulated external system HTTP 503 outage"));
        }
        if (mode == ExternalSimulationMode.SIMULATED_TIMEOUT) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body(ApiResponseWrapper.error("Simulated external system HTTP 504 gateway timeout"));
        }
        WebhookReceiptDto receipt = idempotentWebhookService.processIncomingWebhook(envelope, idempotencyKey);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Webhook acknowledged by external system", receipt));
    }

    /**
     * Updates the operational simulation mode of the external receiver (HEALTHY, TRANSIENT_FAILURE_503, SIMULATED_TIMEOUT).
     *
     * @param mode target ExternalSimulationMode
     * @return HTTP 200 confirmation with active mode
     */
    @PutMapping("/mode")
    @Operation(summary = "Set external system failure simulation mode", description = "Switches the mock external system between HEALTHY, TRANSIENT_FAILURE_503, and SIMULATED_TIMEOUT.")
    @ApiResponse(responseCode = "200", description = "Simulation mode updated")
    public ResponseEntity<ApiResponseWrapper<ExternalSimulationMode>> updateSimulationMode(
            @RequestParam("mode") ExternalSimulationMode mode) {
        idempotentWebhookService.setSimulationMode(mode);
        return ResponseEntity.ok(ApiResponseWrapper.ok("External simulation mode updated", mode));
    }

    /**
     * Returns the current simulation mode of the external receiver.
     *
     * @return HTTP 200 response with current ExternalSimulationMode
     */
    @GetMapping("/mode")
    @Operation(summary = "Get current external simulation mode", description = "Returns the active simulation mode of the mock external receiver.")
    @ApiResponse(responseCode = "200", description = "Current mode returned")
    public ResponseEntity<ApiResponseWrapper<ExternalSimulationMode>> getSimulationMode() {
        return ResponseEntity.ok(ApiResponseWrapper.ok(
                "Current external simulation mode",
                idempotentWebhookService.getSimulationMode()
        ));
    }

    /**
     * Lists all deduplicated events received by the simulated external consumer.
     *
     * @return HTTP 200 response with list of ProcessedWebhookEvent entities
     */
    @GetMapping("/received-events")
    @Operation(summary = "List deduplicated events received by external consumer", description = "Returns all events stored in the external consumer's idempotent inbox.")
    @ApiResponse(responseCode = "200", description = "Received events listed")
    public ResponseEntity<ApiResponseWrapper<List<ProcessedWebhookEvent>>> listReceivedEvents() {
        List<ProcessedWebhookEvent> events = idempotentWebhookService.listProcessedEvents();
        return ResponseEntity.ok(ApiResponseWrapper.ok("Received external events fetched", events));
    }
}
