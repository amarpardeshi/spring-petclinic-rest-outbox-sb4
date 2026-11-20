# SDD Artifact 03 — Event Contract, Idempotency & State Machine Specification

## 1. Event Envelope Specification (CloudEvents 1.0 Inspired JSON)
Every outbound webhook payload and pull-feed item conforms to `IntegrationEventEnvelope`:

```json
{
  "specVersion": "1.0",
  "eventId": "6b2f8d42-9c11-4f88-8b2a-1c3d5e7f9a01",
  "sequenceNumber": 42,
  "eventType": "OWNER_UPDATED",
  "aggregateType": "OWNER",
  "aggregateId": "10",
  "occurredAt": "2026-11-15T15:00:00Z",
  "payloadHash": "a3f5c8e9...",
  "attemptCount": 1,
  "data": {
    "id": 10,
    "firstName": "Amar",
    "lastName": "Pardeshi",
    "address": "Keshavnagar",
    "city": "Pune",
    "telephone": "9876543210"
  }
}
```

## 2. Outbox Lifecycle State Machine
- `PENDING`: Initial state written atomically with the domain mutation (`attemptCount = 0`).
- `FAILED_RETRYABLE`: Delivery attempt failed with a transient error and `attemptCount < maxAttempts` (default `5`). Scheduled for retry at `nextAttemptAt = occurredTime + initialBackoffSeconds * 2^(attemptCount - 1)`.
- `PUBLISHED`: Successfully acknowledged by the external system (`HTTP 2xx`). Terminal success state.
- `DEAD_LETTER`: Exhausted all `maxAttempts` without acknowledgment. Quarantined from automatic polling to prevent poison-pill head-of-line blocking, flagged in Actuator health/metrics, and recoverable via administrative replay endpoints.

## 3. Duplicate Handling & Idempotency Contract
Because network timeouts can occur *after* the external system processes a request but *before* PetClinic receives the HTTP `200 OK` response, **at-least-once** delivery inherently permits duplicate transmissions.
1. **Producer Guarantee**: Retries of the same logical mutation always reuse the original immutable `eventId`, `sequenceNumber`, and `X-Idempotency-Key` header.
2. **Consumer Contract (Implemented in `IdempotentWebhookService`)**:
   - The consumer extracts `eventId` (or `X-Idempotency-Key`).
   - Checks `processed_webhook_events` table for `event_id`.
   - If already present, immediately returns `200 OK` with `"duplicate": true` without re-applying side effects.
   - If absent, persists `ProcessedWebhookEvent` and processes the payload atomically.
