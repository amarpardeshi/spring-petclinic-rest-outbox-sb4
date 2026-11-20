# SDD Artifact 06 — AI Usage & Engineering Judgment Note

## 1. AI Tools Used & Workflow Integration
- **Tools Used**: AI Coding Assistant (Gemini) integrated into a Spec-Driven Development (SDD) workflow.
- **How AI Was Used**:
  1. **Requirements Decomposition & Edge-Case Brainstorming**: Enumerating partial failure modes between relational commits and external webhook notifications.
  2. **Boilerplate & OpenAPI Annotation Acceleration**: Generating repetitive `@Operation`, `@ApiResponse`, and `@Schema` metadata for Swagger UI ([SpringDoc OpenAPI 3.2.0](https://springdoc.org/)).
  3. **Test Case Scaffolding**: Constructing unit and integration test scenarios covering backoff math, dead-letter transitions, and idempotency.

## 2. AI Suggestions Rejected or Significantly Modified

### Rejection 1: Using `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` with `@Async` Instead of a Persistent Outbox Table
- **Initial AI Suggestion**: Use Spring's built-in `@ApplicationEventPublisher` paired with an `@Async @TransactionalEventListener(AFTER_COMMIT)` listener that calls the external webhook directly in memory.
- **Why It Was Rejected**: If the JVM crashes, restarts, or experiences a deployment rollout immediately after the database transaction commits but before the in-memory `@Async` thread completes the HTTP call, the event is **permanently lost without notice**. This directly violates Requirement 2 (*"A temporary failure or unavailability of the external system must not cause a relevant change to be lost without notice"*).
- **Engineering Resolution**: Implemented a database-backed **Transactional Outbox (`outbox_events`)** persisted in the exact same ACID transaction as the `Owner`, `Pet`, and `Visit` mutations.

### Rejection 2: Retrying Failed Events Indefinitely Without a Dead-Letter State or Pull-Feed Fallback
- **Initial AI Suggestion**: Keep failed events in `PENDING` status and retry them on every scheduler tick indefinitely.
- **Why It Was Modified**: Infinite immediate retries create two production hazards: (1) a malformed poison-pill event or long outage causes head-of-line blocking and retry storms, and (2) operators have no clear alerting signal when an event has exceeded its recovery SLA.
- **Engineering Resolution**: Added bounded retries (`maxAttempts = 5`) with exponential backoff (`nextAttemptAt`), a quarantined `DEAD_LETTER` terminal state monitored by `OutboxHealthIndicator`, manual replay REST endpoints, and a cursor-based Pull Feed (`GET /petclinic/api/v1/integration/outbox/feed`) for bulk catch-up.
