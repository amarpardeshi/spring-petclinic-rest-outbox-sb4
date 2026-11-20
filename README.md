# Spring PetClinic REST — Reliable Event Integration (Spring Boot 4.0.9)

## Overview
This project implements the **Backend Engineer — Take-Home Assignment** based on [Spring PetClinic REST](https://github.com/spring-petclinic/spring-petclinic-rest), upgraded to **[Spring Boot 4.0.9](https://spring.io/blog/2026/10/23/spring-boot-4-0-9-available-now/)** and **[SpringDoc OpenAPI 3.2.0](https://springdoc.org/)**.

It solves reliable change propagation for PetClinic aggregates (`Owners`, `Pets`, `Visits`) using the **Transactional Outbox Pattern**, an **Asynchronous Exponential-Backoff Webhook Relay**, an **Idempotent Consumer Inbox**, and a **Hybrid Pull Catch-Up Feed**.

## Key Architectural Highlights
- **Spring Boot 4.0.9 & Java 21**: Uses modern Spring Framework 7 `RestClient`, Jakarta Validation, Spring Data JPA, and Spring Boot Actuator.
- **Strict Code Hygiene**:
  - **SOLID Principles**: Strict separation of domain services, event recording (`DomainEventPublisher`), transport (`ExternalEventTransport`), and relay orchestration (`OutboxRelayService`).
  - **Zero Wildcard (`*`) Imports**: Every Java class imports only the exact classes it uses.
  - **100% Function Documentation**: Every constructor and method includes a top Javadoc description comment.
- **Swagger / OpenAPI 3.1 UI**: Interactive documentation at `http://localhost:9966/petclinic/swagger-ui.html` (and JSON spec at `http://localhost:9966/petclinic/v3/api-docs`).

## Spec-Driven Development (SDD) Artifacts
All SDD artifacts produced during the engineering workflow are located in `docs/sdd/`:
1. [`docs/sdd/01-requirements-and-assumptions.md`](docs/sdd/01-requirements-and-assumptions.md) — Requirements analysis, ambiguity resolution, and explicit assumptions.
2. [`docs/sdd/02-architecture-and-tradeoffs.md`](docs/sdd/02-architecture-and-tradeoffs.md) — ADR and trade-off comparison across 4 integration patterns.
3. [`docs/sdd/03-event-contract-and-specification.md`](docs/sdd/03-event-contract-and-specification.md) — CloudEvents-aligned JSON schema, idempotency contract, and state machine.
4. [`docs/sdd/04-implementation-plan-and-traceability.md`](docs/sdd/04-implementation-plan-and-traceability.md) — SOLID mapping and Requirements-to-Code-to-Test traceability matrix.
5. [`docs/sdd/05-verification-and-test-report.md`](docs/sdd/05-verification-and-test-report.md) — Automated verification strategy and static quality checks.
6. [`docs/sdd/06-ai-usage-and-engineering-judgment.md`](docs/sdd/06-ai-usage-and-engineering-judgment.md) — AI tools used and rejected/modified AI suggestions.

## Prerequisites & Build / Run Instructions
- **Prerequisites**: Java 21+ (compatible with Java 17–25 per [Spring Boot 4.0 Release Notes](https://spring.io/blog/2025/11/20/spring-boot-4-0-0-available-now/)) and Apache Maven 3.9+. No Docker, external broker, or external credentials are required; H2 runs in-memory with pre-seeded PetClinic data.

### 1. Clean Build & Run Automated Tests
```bash
./mvnw clean install
```

### 2. Run Only the Test Suite
```bash
./mvnw test
```

### 3. Run the Application Locally
```bash
./mvnw spring-boot:run
```
Once started, access:
- **Swagger UI**: `http://localhost:9966/petclinic/swagger-ui.html`
- **OpenAPI 3.1 Spec**: `http://localhost:9966/petclinic/v3/api-docs`
- **Actuator Health (with Outbox Health Indicator)**: `http://localhost:9966/petclinic/actuator/health`
- **Outbox Integration & Replay Endpoints**: `http://localhost:9966/petclinic/api/v1/integration/outbox/stats`
- **Simulated External System Control Endpoints**: `http://localhost:9966/petclinic/api/v1/mock-external/mode`
