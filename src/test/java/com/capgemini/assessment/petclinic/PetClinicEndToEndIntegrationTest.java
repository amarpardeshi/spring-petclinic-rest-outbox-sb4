package com.capgemini.assessment.petclinic;

import com.capgemini.assessment.petclinic.domain.dto.OwnerRequestDto;
import com.capgemini.assessment.petclinic.domain.dto.OwnerResponseDto;
import com.capgemini.assessment.petclinic.domain.dto.PetRequestDto;
import com.capgemini.assessment.petclinic.domain.dto.PetResponseDto;
import com.capgemini.assessment.petclinic.domain.dto.VisitRequestDto;
import com.capgemini.assessment.petclinic.domain.dto.VisitResponseDto;
import com.capgemini.assessment.petclinic.domain.model.PetType;
import com.capgemini.assessment.petclinic.domain.service.OwnerService;
import com.capgemini.assessment.petclinic.domain.service.PetService;
import com.capgemini.assessment.petclinic.domain.service.VisitService;
import com.capgemini.assessment.petclinic.integration.dto.IntegrationEventEnvelope;
import com.capgemini.assessment.petclinic.integration.dto.OutboxStatsDto;
import com.capgemini.assessment.petclinic.integration.repository.OutboxEventRepository;
import com.capgemini.assessment.petclinic.integration.service.OutboxRelayService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * End-to-end Spring Boot integration test verifying transactional outbox creation across Owner, Pet, and Visit workflows.
 */
@SpringBootTest
@ActiveProfiles("test")
class PetClinicEndToEndIntegrationTest {

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private PetService petService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxRelayService outboxRelayService;

    /**
     * Clears outbox records prior to each integration test scenario.
     */
    @BeforeEach
    void cleanOutbox() {
        outboxEventRepository.deleteAll();
    }

    /**
     * Verifies that Owner, Pet, and Visit mutations succeed and record outbox events even without external delivery.
     */
    @Test
    void shouldCommitDomainLifecycleChangesAndRecordOutboxEventsAtomically() {
        OwnerResponseDto owner = ownerService.createOwner(new OwnerRequestDto(
                "Amar",
                "Pardeshi",
                "A-403 Nivasa Elevia, Keshavnagar",
                "Pune",
                "9876543210"
        ));
        Assertions.assertNotNull(owner.id());

        PetResponseDto pet = petService.createPet(new PetRequestDto(
                "Simba",
                LocalDate.of(2024, 5, 10),
                PetType.DOG,
                owner.id()
        ));
        Assertions.assertNotNull(pet.id());

        VisitResponseDto visit = visitService.createVisit(new VisitRequestDto(
                LocalDate.of(2026, 11, 15),
                "Annual vaccination checkup",
                "Dr. Helen Leary",
                pet.id()
        ));
        Assertions.assertNotNull(visit.id());

        OutboxStatsDto stats = outboxRelayService.getOutboxStats();
        Assertions.assertEquals(3L, stats.pendingCount());
        Assertions.assertEquals(3L, stats.totalCount());

        List<IntegrationEventEnvelope> feed = outboxRelayService.fetchPullFeed(0L, 10);
        Assertions.assertEquals(3, feed.size());
        Assertions.assertEquals("OWNER_CREATED", feed.get(0).eventType());
        Assertions.assertEquals("PET_CREATED", feed.get(1).eventType());
        Assertions.assertEquals("VISIT_CREATED", feed.get(2).eventType());
    }
}
