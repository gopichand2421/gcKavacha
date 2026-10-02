package com.gckavach.gckavachapp.alert.correlation;

import com.gckavach.gckavachapp.alert.repository.AlertCorrelationRepository;
import com.gckavach.gckavachapp.alert.service.DefaultAlertCorrelationPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAlertCorrelationPersistenceServiceTest {

    @Mock
    private AlertCorrelationRepository repository;

    private DefaultAlertCorrelationPersistenceService service;

    @BeforeEach
    void setUp() {

        service =
                new DefaultAlertCorrelationPersistenceService(
                        repository
                );
    }

    @Test
    void shouldSaveCorrelation() {

        AlertCorrelation correlation =
                createCorrelation(
                        "alert-001",
                        "alert-002"
                );

        when(repository.save(any(AlertCorrelation.class)))
                .thenReturn(correlation);

        AlertCorrelation result =
                service.save(correlation);

        assertEquals(
                correlation,
                result
        );

        verify(repository)
                .save(any(AlertCorrelation.class));
    }

    @Test
    void shouldFindCorrelationsForAlert() {

        AlertCorrelation correlation =
                createCorrelation(
                        "alert-001",
                        "alert-002"
                );

        when(
                repository
                        .findByPrimaryAlertIdOrRelatedAlertId(
                                "alert-001",
                                "alert-001"
                        )
        ).thenReturn(List.of(correlation));

        List<AlertCorrelation> result =
                service.findByAlertId("alert-001");

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "alert-001",
                result.get(0).getPrimaryAlertId()
        );
    }

    @Test
    void shouldReturnTrueWhenCorrelationExists() {

        when(
                repository
                        .existsByPrimaryAlertIdAndRelatedAlertId(
                                "alert-001",
                                "alert-002"
                        )
        ).thenReturn(true);

        assertTrue(
                service.exists(
                        "alert-001",
                        "alert-002"
                )
        );
    }

    @Test
    void shouldReturnFalseWhenCorrelationDoesNotExist() {

        when(
                repository
                        .existsByPrimaryAlertIdAndRelatedAlertId(
                                "alert-001",
                                "alert-002"
                        )
        ).thenReturn(false);

        assertFalse(
                service.exists(
                        "alert-001",
                        "alert-002"
                )
        );
    }

    @Test
    void shouldRejectNullCorrelation() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.save(null)
        );
    }

    @Test
    void shouldRejectSelfCorrelation() {

        AlertCorrelation correlation =
                createCorrelation(
                        "alert-001",
                        "alert-001"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.save(correlation)
        );
    }

    @Test
    void shouldRejectBlankAlertId() {

        AlertCorrelation correlation =
                createCorrelation(
                        "",
                        "alert-002"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.save(correlation)
        );
    }

    private AlertCorrelation createCorrelation(
            String primaryAlertId,
            String relatedAlertId) {

        return new AlertCorrelation(
                primaryAlertId,
                relatedAlertId,
                AlertCorrelationType
                        .SAME_SERVICE_ENVIRONMENT_TIME_WINDOW
                        .name(),
                300
        );
    }
}