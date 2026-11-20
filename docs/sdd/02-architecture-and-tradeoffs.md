# SDD Artifact 02 — Architecture Decision Record (ADR) & Integration Trade-Offs

## 1. Context
PetClinic must notify an external system of changes to `Owners`, `Pets`, and `Visits` while guaranteeing at-least-once delivery, fault isolation, observability, and recoverability on [Spring Boot 4.0.9](https://spring.io/blog/2026/10/23/spring-boot-4-0-9-available-now/).

## 2. Evaluated Integration Alternatives

| Criterion | Option A: Synchronous HTTP / Dual-Write | Option B: In-Memory `@Async` / `@TransactionalEventListener` | Option C: External Broker + CDC (Kafka + Debezium) | **Option D (Selected): Transactional Outbox + Async Webhook Relay + Pull Feed** |
| :--- | :--- | :--- | :--- | :--- |
| **Zero Lost Events on Crash** | No (if HTTP fails after DB commit, or DB rolls back after HTTP call) | No (JVM crash between DB commit and async thread loses event) | Yes | **Yes** (Atomically committed in same local ACID transaction) |
| **Main Workflow Isolation** | Poor (External latency/downtime blocks PetClinic CRUD) | High | High | **High** (Zero network I/O in CRUD path) |
| **Operational Simplicity** | High | High | Low (Requires ZooKeeper/KRaft, Kafka Connect, Debezium) | **High** (Runs anywhere Spring Boot + SQL runs; zero extra infra for assessment) |
| **Observability & Replay** | Low (Lost in logs unless custom store built) | Low | Medium (Offset manipulation required) | **High** (SQL-queryable outbox state, Actuator metrics, 1-click REST replay) |

## 3. Selected Architecture: Transactional Outbox + Hybrid Push/Pull
1. **Transactional Write Phase**:
   - `OwnerController` / `PetController` / `VisitController` invokes `OwnerService` / `PetService` / `VisitService`.
   - Within a single `@Transactional` boundary, the service persists the JPA entity (`Owner`, `Pet`, `Visit`) and delegates to `DomainEventPublisher` (`OutboxDomainEventPublisher`), which serializes a snapshot payload and inserts an `OutboxEvent` row with status `PENDING`.
2. **Asynchronous Relay Phase (`OutboxRelayService` & `OutboxRelayScheduler`)**:
   - Polls `outbox_events` where `status IN (PENDING, FAILED_RETRYABLE)` and `next_attempt_at <= NOW()` ordered by `id ASC`.
   - Dispatches each event over HTTP `POST` via `ExternalEventTransport` (`RestClientWebhookTransport`) with headers:
     - `X-Idempotency-Key: <eventId>`
     - `X-Event-Type: <eventType>`
     - `X-Payload-SHA256: <payloadHash>`
3. **Failure, Backoff & Dead-Letter Phase**:
   - On HTTP `2xx`: transitions to `PUBLISHED`, records `publishedAt`, increments `petclinic.outbox.published`.
   - On transient error (`5xx` or network error) when `attemptCount < maxAttempts`: transitions to `FAILED_RETRYABLE`, computes `nextAttemptAt = now + baseDelaySeconds * 2^(attemptCount - 1)`, records `lastError`.
   - On terminal error (`attemptCount >= maxAttempts`): transitions to `DEAD_LETTER`, exposes degraded status in `OutboxHealthIndicator`, and awaits operator replay via `POST /petclinic/api/v1/integration/outbox/{eventId}/retry`.
