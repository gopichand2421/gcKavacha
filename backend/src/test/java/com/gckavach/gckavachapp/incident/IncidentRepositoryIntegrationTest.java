package com.gckavach.gckavachapp.incident;

import com.gckavach.gckavachapp.incident.domain.Incident;
import com.gckavach.gckavachapp.incident.domain.IncidentSeverity;
import com.gckavach.gckavachapp.incident.domain.IncidentStatus;
import com.gckavach.gckavachapp.incident.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
@ActiveProfiles("test")
class IncidentRepositoryIntegrationTest {

    @Autowired
    private IncidentRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldSaveIncident() {

        Incident incident =
                createIncident(
                        "INC-2026-000001",
                        "Payment service degradation",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "prod"
                );

        Incident saved =
                repository.save(incident);

        assertNotNull(saved.getId());

        assertEquals(
                "INC-2026-000001",
                saved.getIncidentNumber()
        );

        assertEquals(
                "Payment service degradation",
                saved.getTitle()
        );

        assertEquals(
                IncidentSeverity.HIGH,
                saved.getSeverity()
        );

        assertEquals(
                IncidentStatus.OPEN,
                saved.getStatus()
        );

        assertEquals(
                "payment-service",
                saved.getServiceName()
        );

        assertEquals(
                "prod",
                saved.getEnvironment()
        );
    }

    @Test
    void shouldFindByIncidentNumber() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Payment failure",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "prod"
                )
        );

        var result =
                repository.findByIncidentNumber(
                        "INC-2026-000001"
                );

        assertTrue(result.isPresent());

        assertEquals(
                "INC-2026-000001",
                result.get().getIncidentNumber()
        );
    }

    @Test
    void shouldReturnEmptyForUnknownIncidentNumber() {

        var result =
                repository.findByIncidentNumber(
                        "INC-2026-999999"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckIncidentNumberExistence() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Payment failure",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "prod"
                )
        );

        assertTrue(
                repository.existsByIncidentNumber(
                        "INC-2026-000001"
                )
        );

        assertFalse(
                repository.existsByIncidentNumber(
                        "INC-2026-000002"
                )
        );
    }

    @Test
    void shouldPreventDuplicateIncidentNumber() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Payment failure",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "prod"
                )
        );

        assertThrows(
                DuplicateKeyException.class,
                () -> repository.save(
                        createIncident(
                                "INC-2026-000001",
                                "Another payment failure",
                                IncidentSeverity.CRITICAL,
                                "payment-service",
                                "prod"
                        )
                )
        );
    }

    @Test
    void shouldFindByStatus() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Payment failure",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "prod"
                )
        );

        Incident resolvedIncident =
                createIncident(
                        "INC-2026-000002",
                        "Database issue",
                        IncidentSeverity.MEDIUM,
                        "database-service",
                        "prod"
                );

        resolvedIncident.setStatus(
                IncidentStatus.RESOLVED
        );

        repository.save(resolvedIncident);

        var result =
                repository.findByStatus(
                        IncidentStatus.OPEN,
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        )
                );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "INC-2026-000001",
                result.getContent()
                        .get(0)
                        .getIncidentNumber()
        );
    }

    @Test
    void shouldFindBySeverity() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Payment failure",
                        IncidentSeverity.CRITICAL,
                        "payment-service",
                        "prod"
                )
        );

        repository.save(
                createIncident(
                        "INC-2026-000002",
                        "Cache issue",
                        IncidentSeverity.LOW,
                        "cache-service",
                        "prod"
                )
        );

        var result =
                repository.findBySeverity(
                        IncidentSeverity.CRITICAL,
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        )
                );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                IncidentSeverity.CRITICAL,
                result.getContent()
                        .get(0)
                        .getSeverity()
        );
    }

    @Test
    void shouldFindByServiceName() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Payment failure",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "prod"
                )
        );

        repository.save(
                createIncident(
                        "INC-2026-000002",
                        "Order failure",
                        IncidentSeverity.HIGH,
                        "order-service",
                        "prod"
                )
        );

        var result =
                repository.findByServiceName(
                        "payment-service",
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        )
                );

        assertEquals(
                1,
                result.getTotalElements()
        );
    }

    @Test
    void shouldFindByEnvironment() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Production issue",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "prod"
                )
        );

        repository.save(
                createIncident(
                        "INC-2026-000002",
                        "QA issue",
                        IncidentSeverity.MEDIUM,
                        "payment-service",
                        "qa"
                )
        );

        var result =
                repository.findByEnvironment(
                        "prod",
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        )
                );

        assertEquals(
                1,
                result.getTotalElements()
        );
    }

    @Test
    void shouldFindByServiceAndEnvironment() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Production payment issue",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "prod"
                )
        );

        repository.save(
                createIncident(
                        "INC-2026-000002",
                        "QA payment issue",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "qa"
                )
        );

        var result =
                repository.findByServiceNameAndEnvironment(
                        "payment-service",
                        "prod",
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        )
                );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "prod",
                result.getContent()
                        .get(0)
                        .getEnvironment()
        );
    }

    @Test
    void shouldFindByStatusAndSeverity() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Critical payment issue",
                        IncidentSeverity.CRITICAL,
                        "payment-service",
                        "prod"
                )
        );

        repository.save(
                createIncident(
                        "INC-2026-000002",
                        "High payment issue",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "prod"
                )
        );

        var result =
                repository.findByStatusAndSeverity(
                        IncidentStatus.OPEN,
                        IncidentSeverity.CRITICAL,
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        )
                );

        assertEquals(
                1,
                result.getTotalElements()
        );
    }

    @Test
    void shouldFindByStatusAndServiceName() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Payment issue",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "prod"
                )
        );

        var result =
                repository.findByStatusAndServiceName(
                        IncidentStatus.OPEN,
                        "payment-service",
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        )
                );

        assertEquals(
                1,
                result.getTotalElements()
        );
    }

    @Test
    void shouldFindByStatusAndEnvironment() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Production issue",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "prod"
                )
        );

        var result =
                repository.findByStatusAndEnvironment(
                        IncidentStatus.OPEN,
                        "prod",
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        )
                );

        assertEquals(
                1,
                result.getTotalElements()
        );
    }

    @Test
    void shouldFindByStatusSeverityAndService() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Critical payment issue",
                        IncidentSeverity.CRITICAL,
                        "payment-service",
                        "prod"
                )
        );

        var result =
                repository.findByStatusAndSeverityAndServiceName(
                        IncidentStatus.OPEN,
                        IncidentSeverity.CRITICAL,
                        "payment-service",
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        )
                );

        assertEquals(
                1,
                result.getTotalElements()
        );
    }

    @Test
    void shouldFindByAllSupportedFilters() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Critical production payment issue",
                        IncidentSeverity.CRITICAL,
                        "payment-service",
                        "prod"
                )
        );

        repository.save(
                createIncident(
                        "INC-2026-000002",
                        "Critical QA payment issue",
                        IncidentSeverity.CRITICAL,
                        "payment-service",
                        "qa"
                )
        );

        var result =
                repository
                        .findByStatusAndSeverityAndServiceNameAndEnvironment(
                                IncidentStatus.OPEN,
                                IncidentSeverity.CRITICAL,
                                "payment-service",
                                "prod",
                                org.springframework.data.domain.PageRequest.of(
                                        0,
                                        20
                                )
                        );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "INC-2026-000001",
                result.getContent()
                        .get(0)
                        .getIncidentNumber()
        );
    }

    @Test
    void shouldDeleteIncident() {

        Incident incident =
                repository.save(
                        createIncident(
                                "INC-2026-000001",
                                "Payment failure",
                                IncidentSeverity.HIGH,
                                "payment-service",
                                "prod"
                        )
                );

        repository.deleteById(
                incident.getId()
        );

        assertTrue(
                repository
                        .findById(incident.getId())
                        .isEmpty()
        );
    }

    @Test
    void shouldReturnAllIncidentsUsingFindAll() {

        repository.save(
                createIncident(
                        "INC-2026-000001",
                        "Payment issue",
                        IncidentSeverity.HIGH,
                        "payment-service",
                        "prod"
                )
        );

        repository.save(
                createIncident(
                        "INC-2026-000002",
                        "Order issue",
                        IncidentSeverity.MEDIUM,
                        "order-service",
                        "prod"
                )
        );

        List<Incident> incidents =
                repository.findAll();

        assertEquals(
                2,
                incidents.size()
        );
    }

    private Incident createIncident(
            String incidentNumber,
            String title,
            IncidentSeverity severity,
            String serviceName,
            String environment) {

        return new Incident(
                incidentNumber,
                title,
                "Test incident description",
                severity,
                serviceName,
                environment
        );
    }
}