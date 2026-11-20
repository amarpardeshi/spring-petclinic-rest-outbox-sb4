package com.capgemini.assessment.petclinic.integration.controller;

import com.capgemini.assessment.petclinic.common.constants.ApiConstants;
import com.capgemini.assessment.petclinic.common.dto.ApiResponseWrapper;
import com.capgemini.assessment.petclinic.integration.dto.IntegrationEventEnvelope;
import com.capgemini.assessment.petclinic.integration.dto.OutboxEventResponseDto;
import com.capgemini.assessment.petclinic.integration.dto.OutboxStatsDto;
import com.capgemini.assessment.petclinic.integration.model.OutboxStatus;
import com.capgemini.assessment.petclinic.integration.service.OutboxRelayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Operational REST controller for inspecting, polling, and recovering Transactional Outbox integration events.
 */
@RestController
@RequestMapping(ApiConstants.OUTBOX_PATH)
@Tag(name = "04. Outbox Integration & Recovery API", description = "Observability, Pull Feed Catch-Up, and Dead-Letter Replay endpoints")
public class OutboxIntegrationController {

    private final OutboxRelayService outboxRelayService;

    /**
     * Constructs OutboxIntegrationController with OutboxRelayService dependency.
     *
     * @param outboxRelayService service managing outbox telemetry, relay, and replay
     */
    public OutboxIntegrationController(OutboxRelayService outboxRelayService) {
        this.outboxRelayService = outboxRelayService;
    }

    /**
     * Returns aggregated counts of outbox events across PENDING, FAILED_RETRYABLE, PUBLISHED, and DEAD_LETTER states.
     *
     * @return HTTP 200 response with OutboxStatsDto
     */
    @GetMapping("/stats")
    @Operation(summary = "Get Outbox status telemetry", description = "Returns counts of PENDING, FAILED_RETRYABLE, PUBLISHED, and DEAD_LETTER events.")
    @ApiResponse(responseCode = "200", description = "Telemetry retrieved")
    public ResponseEntity<ApiResponseWrapper<OutboxStatsDto>> getStats() {
        OutboxStatsDto stats = outboxRelayService.getOutboxStats();
        return ResponseEntity.ok(ApiResponseWrapper.ok("Outbox telemetry fetched", stats));
    }

    /**
     * Lists outbox events optionally filtered by lifecycle status.
     *
     * @param status optional OutboxStatus filter
     * @return HTTP 200 response with list of OutboxEventResponseDto
     */
    @GetMapping("/events")
    @Operation(summary = "List Outbox events", description = "Lists persistent outbox events filtered optionally by status (e.g., DEAD_LETTER, FAILED_RETRYABLE).")
    @ApiResponse(responseCode = "200", description = "Outbox events listed")
    public ResponseEntity<ApiResponseWrapper<List<OutboxEventResponseDto>>> listEvents(
            @Parameter(description = "Optional status filter")
            @RequestParam(name = "status", required = false) OutboxStatus status) {
        List<OutboxEventResponseDto> events = outboxRelayService.listEvents(status);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Outbox events fetched", events));
    }

    /**
     * Cursor-paginated Pull Feed endpoint allowing external consumers to pull changes directly after an outage.
     *
     * @param afterSequenceId last processed sequence ID cursor
     * @param limit maximum number of events to return
     * @return HTTP 200 response with ordered IntegrationEventEnvelope list
     */
    @GetMapping("/feed")
    @Operation(summary = "Pull Catch-Up Event Feed", description = "Cursor-paginated pull feed so external consumers can catch up on changes by sequence ID.")
    @ApiResponse(responseCode = "200", description = "Feed envelopes returned")
    public ResponseEntity<ApiResponseWrapper<List<IntegrationEventEnvelope>>> fetchFeed(
            @Parameter(description = "Return events with sequenceNumber strictly greater than this cursor")
            @RequestParam(name = "afterSequenceId", defaultValue = "0") Long afterSequenceId,
            @Parameter(description = "Maximum batch size (1-100)")
            @RequestParam(name = "limit", defaultValue = "50") int limit) {
        List<IntegrationEventEnvelope> feed = outboxRelayService.fetchPullFeed(afterSequenceId, limit);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Integration event feed fetched", feed));
    }

    /**
     * Triggers an immediate synchronous relay pass for all due PENDING and FAILED_RETRYABLE events.
     *
     * @return HTTP 200 response indicating how many events were published
     */
    @PostMapping("/trigger-relay")
    @Operation(summary = "Trigger immediate Outbox relay pass", description = "Immediately polls and dispatches due PENDING and FAILED_RETRYABLE events.")
    @ApiResponse(responseCode = "200", description = "Relay pass completed")
    public ResponseEntity<ApiResponseWrapper<Integer>> triggerRelayNow() {
        int publishedCount = outboxRelayService.processDueEventsBatch();
        return ResponseEntity.ok(ApiResponseWrapper.ok("Relay batch completed", publishedCount));
    }

    /**
     * Manually retries a single failed or dead-lettered event by its eventId.
     *
     * @param eventId unique UUID of the outbox event to replay
     * @return HTTP 200 response with updated OutboxEventResponseDto
     */
    @PostMapping("/{eventId}/retry")
    @Operation(summary = "Retry a single failed/dead-lettered event", description = "Resets a FAILED_RETRYABLE or DEAD_LETTER event and immediately attempts redelivery.")
    @ApiResponse(responseCode = "200", description = "Event retry executed")
    @ApiResponse(responseCode = "404", description = "Event ID not found")
    public ResponseEntity<ApiResponseWrapper<OutboxEventResponseDto>> retrySingleEvent(
            @PathVariable("eventId") String eventId) {
        OutboxEventResponseDto result = outboxRelayService.retryEventById(eventId);
        return ResponseEntity.ok(ApiResponseWrapper.ok("Single event retry executed", result));
    }

    /**
     * Requeues and retries all quarantined DEAD_LETTER events.
     *
     * @return HTTP 200 response with the count of requeued dead-letter events
     */
    @PostMapping("/retry-all-dead-letter")
    @Operation(summary = "Replay all DEAD_LETTER events", description = "Resets all quarantined DEAD_LETTER events to PENDING and triggers an immediate relay batch.")
    @ApiResponse(responseCode = "200", description = "Dead-letter events requeued")
    public ResponseEntity<ApiResponseWrapper<Integer>> retryAllDeadLetterEvents() {
        int count = outboxRelayService.retryAllDeadLetterEvents();
        return ResponseEntity.ok(ApiResponseWrapper.ok("Requeued dead-letter events", count));
    }
}
