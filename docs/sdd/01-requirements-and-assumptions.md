# SDD Artifact 01 — Requirements Analysis, Ambiguity Resolution & Assumptions

## 1. Problem Statement
The [Spring PetClinic REST](https://github.com/spring-petclinic/spring-petclinic-rest) backend manages veterinary clinic records (`Owners`, `Pets`, and `Visits`). An external consumer system requires reliable notifications whenever relevant domain state changes occur, without coupling PetClinic's core request-response latency or availability to the external system.

## 2. Functional & Reliability Requirements Mapping
| ID | Requirement | Design Resolution |
| :--- | :--- | :--- |
| **FR-01** | Relevant changes to PetClinic data (`Owners`, `Pets`, `Visits`) must become available to the external system. | Domain services emit structured domain mutation events (`OWNER_CREATED`, `OWNER_UPDATED`, `OWNER_DELETED`, `PET_CREATED`, `PET_UPDATED`, `VISIT_CREATED`, `VISIT_UPDATED`) inside the same ACID database transaction via `DomainEventRecorder`. |
| **FR-02** | Temporary failure or unavailability of the external system must not lose changes without notice. | Events are persisted first in the `outbox_events` table (**Transactional Outbox Pattern**). A background relay worker (`OutboxRelayScheduler`) polls pending/retryable records and retries with exponential backoff. |
| **FR-03** | Duplicate delivery can occur; handle duplicates explicitly or document expected consumer behavior. | Every event carries a deterministic UUID `eventId`, SHA-256 `payloadHash`, and `X-Idempotency-Key` header. Our built-in external receiver (`ExternalSystemSimulationController`) enforces inbox deduplication (`processed_webhook_events`). |
| **FR-04** | Main PetClinic workflow must not become fragile when the external system is unavailable. | PetClinic REST controllers never make outbound network calls during user transactions. All external HTTP calls occur asynchronously on a background thread pool. |
| **FR-05** | Failures must be observable and recoverable. | Spring Boot Actuator health indicator (`OutboxHealthIndicator`), Micrometer counters/gauges, audit columns (`last_error`, `attempt_count`), Dead-Letter quarantine (`DEAD_LETTER`), and manual replay REST endpoints (`/api/v1/integration/outbox/{eventId}/retry`). |

## 3. Ambiguities Identified & Documented Assumptions
| Ambiguity in Prompt | Documented Assumption & Engineering Justification |
| :--- | :--- |
| **A-01: Which entities constitute "relevant changes"?** | Assumed `Owner`, `Pet`, and `Visit` lifecycle mutations (`CREATED`, `UPDATED`, `DELETED`) are the primary transactional aggregates, while reference tables (`Specialty`, `PetType`) change rarely. Reference entities can be added without modifying existing relay code (Open/Closed Principle). |
| **A-02: Push vs. Pull preference of the external system?** | Implemented a **Hybrid Push + Pull** model: primary low-latency delivery via asynchronous HTTP Webhook Push (`RestClient` in [Spring Boot 4.0.9](https://spring.io/blog/2026/10/23/spring-boot-4-0-9-available-now/)), complemented by a cursor-paginated Pull Catch-Up Feed (`GET /petclinic/api/v1/integration/outbox/feed?afterSequenceId=...`) so downstream systems can self-heal after prolonged outages. |
| **A-03: Ordering guarantees across events?** | Outbox records use a monotonically increasing database sequence (`id`) and preserve per-aggregate chronological order. When an earlier event for an aggregate is still `FAILED_RETRYABLE`, consumers can reconcile state using the monotonic `sequenceNumber` and `occurredAt` timestamp included in the envelope. |
| **A-04: External infrastructure availability during assessment evaluation?** | To ensure `./mvnw clean install` and `./mvnw spring-boot:run` run self-contained with zero external Docker/broker dependencies, the project uses an embedded H2 database in PostgreSQL/ANSI-compatible mode and includes an in-process **Simulated External System Receiver** (`/petclinic/api/v1/mock-external/webhook`) with configurable chaos/failure modes (`HEALTHY`, `TRANSIENT_FAILURE_503`, `TIMEOUT`) toggleable via Swagger UI. |
