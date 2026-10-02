package com.gckavach.gckavachapp.alert.correlation;

import com.gckavach.gckavachapp.alert.domain.Alert;
import com.gckavach.gckavachapp.alert.domain.AlertSeverity;
import com.gckavach.gckavachapp.alert.domain.AlertStatus;
import com.gckavach.gckavachapp.alert.repository.AlertRepository;
import com.gckavach.gckavachapp.alert.service.DefaultAlertCorrelationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAlertCorrelationServiceTest {

    @Mock
    private AlertRepository alertRepository;

    private DefaultAlertCorrelationService service;

    @BeforeEach
    void setUp() {
        service = new DefaultAlertCorrelationService(
                alertRepository
        );
    }

    @Test
    void shouldCorrelateAlertsFromSameServiceAndEnvironment() {

        Alert currentAlert = createAlert(
                "alert-002",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:05:00Z")
        );

        Alert previousAlert = createAlert(
                "alert-001",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:00:00Z")
        );

        when(
                alertRepository
                        .findByServiceNameAndEnvironmentAndStartedAtBetween(
                                eq("payment-service"),
                                eq("prod"),
                                any(Instant.class),
                                any(Instant.class)
                        )
        ).thenReturn(
                List.of(
                        previousAlert,
                        currentAlert
                )
        );

        AlertCorrelationResult result =
                service.correlate(
                        currentAlert,
                        Duration.ofMinutes(10)
                );

        assertTrue(result.correlated());

        assertEquals(
                1,
                result.relatedAlerts().size()
        );

        assertEquals(
                "alert-001",
                result.primaryAlert().getId()
        );
    }

    @Test
    void shouldNotCorrelateDifferentService() {

        Alert currentAlert = createAlert(
                "alert-002",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:05:00Z")
        );

        when(
                alertRepository
                        .findByServiceNameAndEnvironmentAndStartedAtBetween(
                                eq("payment-service"),
                                eq("prod"),
                                any(Instant.class),
                                any(Instant.class)
                        )
        ).thenReturn(List.of());

        AlertCorrelationResult result =
                service.correlate(
                        currentAlert,
                        Duration.ofMinutes(10)
                );

        assertFalse(result.correlated());
        assertTrue(result.relatedAlerts().isEmpty());
    }

    @Test
    void shouldNotCorrelateDifferentEnvironment() {

        Alert currentAlert = createAlert(
                "alert-002",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:05:00Z")
        );

        when(
                alertRepository
                        .findByServiceNameAndEnvironmentAndStartedAtBetween(
                                eq("payment-service"),
                                eq("prod"),
                                any(Instant.class),
                                any(Instant.class)
                        )
        ).thenReturn(List.of());

        AlertCorrelationResult result =
                service.correlate(
                        currentAlert,
                        Duration.ofMinutes(10)
                );

        assertFalse(result.correlated());
    }

    @Test
    void shouldExcludeCurrentAlertFromRelatedAlerts() {

        Alert currentAlert = createAlert(
                "alert-001",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:05:00Z")
        );

        when(
                alertRepository
                        .findByServiceNameAndEnvironmentAndStartedAtBetween(
                                eq("payment-service"),
                                eq("prod"),
                                any(Instant.class),
                                any(Instant.class)
                        )
        ).thenReturn(List.of(currentAlert));

        AlertCorrelationResult result =
                service.correlate(
                        currentAlert,
                        Duration.ofMinutes(10)
                );

        assertFalse(result.correlated());

        assertTrue(
                result.relatedAlerts().isEmpty()
        );
    }

    @Test
    void shouldReturnMultipleRelatedAlerts() {

        Alert currentAlert = createAlert(
                "alert-003",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:10:00Z")
        );

        Alert alertOne = createAlert(
                "alert-001",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:01:00Z")
        );

        Alert alertTwo = createAlert(
                "alert-002",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:05:00Z")
        );

        when(
                alertRepository
                        .findByServiceNameAndEnvironmentAndStartedAtBetween(
                                eq("payment-service"),
                                eq("prod"),
                                any(Instant.class),
                                any(Instant.class)
                        )
        ).thenReturn(
                List.of(
                        currentAlert,
                        alertOne,
                        alertTwo
                )
        );

        AlertCorrelationResult result =
                service.correlate(
                        currentAlert,
                        Duration.ofMinutes(10)
                );

        assertTrue(result.correlated());

        assertEquals(
                2,
                result.relatedAlerts().size()
        );

        assertEquals(
                "alert-001",
                result.primaryAlert().getId()
        );
    }

    @Test
    void shouldSortRelatedAlertsByTimeDifference() {

        Alert currentAlert = createAlert(
                "alert-003",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:10:00Z")
        );

        Alert olderAlert = createAlert(
                "alert-001",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:01:00Z")
        );

        Alert closerAlert = createAlert(
                "alert-002",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:09:00Z")
        );

        when(
                alertRepository
                        .findByServiceNameAndEnvironmentAndStartedAtBetween(
                                eq("payment-service"),
                                eq("prod"),
                                any(Instant.class),
                                any(Instant.class)
                        )
        ).thenReturn(
                List.of(
                        olderAlert,
                        closerAlert
                )
        );

        AlertCorrelationResult result =
                service.correlate(
                        currentAlert,
                        Duration.ofMinutes(10)
                );

        assertEquals(
                "alert-002",
                result.relatedAlerts()
                        .get(0)
                        .alert()
                        .getId()
        );

        assertEquals(
                "alert-001",
                result.relatedAlerts()
                        .get(1)
                        .alert()
                        .getId()
        );
    }

    @Test
    void shouldRejectNullAlert() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.correlate(
                        null,
                        Duration.ofMinutes(10)
                )
        );
    }

    @Test
    void shouldRejectNullCorrelationWindow() {

        Alert alert = createAlert(
                "alert-001",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:00:00Z")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.correlate(
                        alert,
                        null
                )
        );
    }

    @Test
    void shouldRejectZeroCorrelationWindow() {

        Alert alert = createAlert(
                "alert-001",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:00:00Z")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.correlate(
                        alert,
                        Duration.ZERO
                )
        );
    }

    @Test
    void shouldRejectNegativeCorrelationWindow() {

        Alert alert = createAlert(
                "alert-001",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:00:00Z")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.correlate(
                        alert,
                        Duration.ofMinutes(-1)
                )
        );
    }

    @Test
    void shouldQueryRepositoryUsingAlertServiceAndEnvironment() {

        Alert currentAlert = createAlert(
                "alert-001",
                "payment-service",
                "prod",
                Instant.parse("2026-10-02T10:00:00Z")
        );

        when(
                alertRepository
                        .findByServiceNameAndEnvironmentAndStartedAtBetween(
                                eq("payment-service"),
                                eq("prod"),
                                any(Instant.class),
                                any(Instant.class)
                        )
        ).thenReturn(List.of());

        service.correlate(
                currentAlert,
                Duration.ofMinutes(10)
        );

        verify(alertRepository)
                .findByServiceNameAndEnvironmentAndStartedAtBetween(
                        eq("payment-service"),
                        eq("prod"),
                        any(Instant.class),
                        any(Instant.class)
                );
    }

    private Alert createAlert(
            String id,
            String serviceName,
            String environment,
            Instant startedAt) {

        Alert alert = new Alert(
                "external-" + id,
                "Test alert",
                "Test description",
                "test-source",
                AlertSeverity.HIGH,
                serviceName,
                environment,
                "fingerprint-" + id
        );

        alert.setId(id);
        alert.setStartedAt(startedAt);
        alert.setStatus(AlertStatus.OPEN);

        return alert;
    }
}