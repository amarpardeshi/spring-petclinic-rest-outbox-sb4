# SDD Artifact 04 — Work Breakdown, SOLID Mapping & Traceability Matrix

## 1. SOLID Principles Applied Across the Codebase
- **Single Responsibility Principle (SRP)**:
  - Domain persistence (`OwnerServiceImpl`, `PetServiceImpl`, `VisitServiceImpl`) is separated from outbox serialization (`OutboxDomainEventPublisher`), background scheduling (`OutboxRelayScheduler`), retry state transitions (`OutboxRelayServiceImpl`), and HTTP transport (`RestClientWebhookTransport`).
- **Open/Closed Principle (OCP)**:
  - New domain aggregates (e.g., `Vet`, `Specialty`) or new event types can be published through `DomainEventPublisher` without modifying `OutboxRelayServiceImpl` or `RestClientWebhookTransport`.
- **Liskov Substitution Principle (LSP)**:
  - Any implementation of `ExternalEventTransport` (e.g., `RestClientWebhookTransport`, test doubles, or a future Kafka/AMQP adapter) honors the transport delivery contract (`TransportDeliveryResult`) transparently.
- **Interface Segregation Principle (ISP)**:
  - Narrow interfaces separate domain event recording (`DomainEventPublisher`), outbound transport (`ExternalEventTransport`), and operational administration (`OutboxRelayService`) so domain services only depend on `DomainEventPublisher`.
- **Dependency Inversion Principle (DIP)**:
  - High-level domain services depend on the `DomainEventPublisher` abstraction rather than JPA `OutboxEventRepository` or HTTP `RestClient` details. All dependencies are constructor-injected (`final` fields).

## 2. Requirements-to-Implementation-to-Tests Traceability Matrix
| Requirement | Primary Classes | Automated Verification Test |
| :--- | :--- | :--- |
| **FR-01: Capture Owner/Pet/Visit changes** | `OwnerServiceImpl`, `PetServiceImpl`, `VisitServiceImpl`, `OutboxDomainEventPublisher` | `PetClinicEndToEndIntegrationTest` |
| **FR-02: No silent loss during outage** | `OutboxEvent`, `OutboxRelayServiceImpl`, `OutboxRelayScheduler` | `OutboxRelayServiceTest`, `PetClinicEndToEndIntegrationTest` |
| **FR-03: Explicit duplicate handling** | `IdempotentWebhookService`, `ProcessedWebhookEvent`, `HashUtils` | `IdempotentWebhookServiceTest` |
| **FR-04: Main workflow isolation** | `@Transactional` boundary in `OwnerServiceImpl` + async outbox | `PetClinicEndToEndIntegrationTest#shouldCommitOwnerEvenWhenExternalSystemIsDown` |
| **FR-05: Observability & Recovery** | `OutboxHealthIndicator`, `OutboxIntegrationController` | `OutboxRelayServiceTest#shouldTransitionToDeadLetterAndAllowManualReplay` |
