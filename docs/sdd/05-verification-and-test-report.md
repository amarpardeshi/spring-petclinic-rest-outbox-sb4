# SDD Artifact 05 — Verification Strategy & Test Matrix

## 1. Automated Test Suite Overview
The test suite verifies both unit-level state transitions and full Spring Boot integration behavior:
1. **`OutboxRelayServiceTest` (Unit Test with Mockito)**:
   - Verifies `PENDING -> PUBLISHED` transition when transport returns `200 OK`.
   - Verifies `PENDING -> FAILED_RETRYABLE` with exponential backoff calculation (`2^(attempt-1)`) when transport fails transiently.
   - Verifies `FAILED_RETRYABLE -> DEAD_LETTER` quarantine when `attemptCount` reaches `maxAttempts`.
   - Verifies manual operator recovery (`retryEvent` and `retryAllDeadLetterEvents`) resets `DEAD_LETTER -> PENDING` and redelivers once transport recovers.
2. **`IdempotentWebhookServiceTest` (Unit Test)**:
   - Verifies first-time delivery records the event and returns `duplicate = false`.
   - Verifies duplicate re-delivery with the same `eventId` is suppressed idempotently (`duplicate = true`) without throwing errors or duplicating state.
3. **`PetClinicEndToEndIntegrationTest` (`@SpringBootTest` + `MockMvc` Integration Test)**:
   - Verifies creating an `Owner`, `Pet`, and `Visit` via REST endpoints commits domain records and persists corresponding `OutboxEvent` records.
   - Simulates external system outage (`TRANSIENT_FAILURE_503`), proves PetClinic REST mutations still succeed (`201 Created`), triggers relay attempts to verify failure tracking, restores external system health (`HEALTHY`), replays the outbox, and verifies end-to-end delivery and consumer idempotency.

## 2. Static Code Quality Standards Verified
- **Zero Wildcard Imports**: All `.java` source and test files use explicit class-level imports (`import package.ClassName;`); zero `import ...*;` statements exist anywhere in the repository.
- **100% Method Documentation**: Every constructor and method across all Java classes and interfaces includes an explicit descriptive Javadoc block above the declaration.
