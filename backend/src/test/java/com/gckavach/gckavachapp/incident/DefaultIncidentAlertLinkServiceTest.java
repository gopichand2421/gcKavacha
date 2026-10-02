package com.gckavach.gckavachapp.incident;

import com.gckavach.gckavachapp.alert.domain.Alert;
import com.gckavach.gckavachapp.alert.repository.AlertRepository;
import com.gckavach.gckavachapp.common.exception.AlertNotFoundException;
import com.gckavach.gckavachapp.common.exception.IncidentAlertLinkAlreadyExistsException;
import com.gckavach.gckavachapp.common.exception.IncidentAlertLinkNotFoundException;
import com.gckavach.gckavachapp.common.exception.IncidentNotFoundException;
import com.gckavach.gckavachapp.incident.api.IncidentAlertLinkResponse;
import com.gckavach.gckavachapp.incident.domain.Incident;
import com.gckavach.gckavachapp.incident.domain.IncidentAlertLink;
import com.gckavach.gckavachapp.incident.repository.IncidentAlertLinkRepository;
import com.gckavach.gckavachapp.incident.repository.IncidentRepository;
import com.gckavach.gckavachapp.incident.service.DefaultIncidentAlertLinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultIncidentAlertLinkServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private IncidentAlertLinkRepository linkRepository;

    private DefaultIncidentAlertLinkService service;

    @BeforeEach
    void setUp() {
        service = new DefaultIncidentAlertLinkService(
                incidentRepository,
                alertRepository,
                linkRepository
        );
    }

    // ============================================================
    // linkAlertToIncident - SUCCESS
    // ============================================================

    @Test
    void shouldLinkAlertToIncidentSuccessfully() {

        String incidentId = "incident-123";
        String alertId = "alert-123";

        IncidentAlertLink savedLink =
                new IncidentAlertLink(incidentId, alertId);

        when(incidentRepository.findById(incidentId))
                .thenReturn(Optional.of(mockIncident()));

        when(alertRepository.findById(alertId))
                .thenReturn(Optional.of(mockAlert()));

        when(linkRepository.existsByIncidentIdAndAlertId(
                incidentId,
                alertId
        )).thenReturn(false);

        when(linkRepository.save(any(IncidentAlertLink.class)))
                .thenReturn(savedLink);

        IncidentAlertLinkResponse response =
                service.linkAlertToIncident(
                        incidentId,
                        alertId
                );

        assertNotNull(response);
        assertEquals(incidentId, response.incidentId());
        assertEquals(alertId, response.alertId());
        assertEquals(
                savedLink.getId(),
                response.linkId()
        );

        verify(incidentRepository)
                .findById(incidentId);

        verify(alertRepository)
                .findById(alertId);

        verify(linkRepository)
                .existsByIncidentIdAndAlertId(
                        incidentId,
                        alertId
                );

        verify(linkRepository)
                .save(any(IncidentAlertLink.class));
    }

    // ============================================================
    // linkAlertToIncident - INCIDENT NOT FOUND
    // ============================================================

    @Test
    void shouldThrowIncidentNotFoundWhenIncidentDoesNotExist() {

        String incidentId = "unknown-incident";
        String alertId = "alert-123";

        when(incidentRepository.findById(incidentId))
                .thenReturn(Optional.empty());

        IncidentNotFoundException exception =
                assertThrows(
                        IncidentNotFoundException.class,
                        () -> service.linkAlertToIncident(
                                incidentId,
                                alertId
                        )
                );

        assertEquals(
                "Incident not found: " + incidentId,
                exception.getMessage()
        );

        verify(incidentRepository)
                .findById(incidentId);

        verifyNoInteractions(alertRepository);

        verify(linkRepository, never())
                .save(any());
    }

    // ============================================================
    // linkAlertToIncident - ALERT NOT FOUND
    // ============================================================

    @Test
    void shouldThrowAlertNotFoundWhenAlertDoesNotExist() {
        String incidentId = "incident-123";
        String alertId = "alert-999";

        Incident incident = mock(Incident.class);

        when(incidentRepository.findById(incidentId))
                .thenReturn(Optional.of(incident));

        when(alertRepository.findById(alertId))
                .thenReturn(Optional.empty());

        AlertNotFoundException exception = assertThrows(
                AlertNotFoundException.class,
                () -> service.linkAlertToIncident(incidentId, alertId)
        );

        assertEquals("Alert not found: " + alertId, exception.getMessage());

        verify(incidentRepository).findById(incidentId);
        verify(alertRepository).findById(alertId);

        verify(linkRepository, never())
                .existsByIncidentIdAndAlertId(anyString(), anyString());

        verify(linkRepository, never()).save(any());
    }

    // ============================================================
    // linkAlertToIncident - DUPLICATE
    // ============================================================

    @Test
    void shouldThrowExceptionWhenLinkAlreadyExists() {

        String incidentId = "incident-123";
        String alertId = "alert-123";

        when(incidentRepository.findById(incidentId))
                .thenReturn(Optional.of(mockIncident()));

        when(alertRepository.findById(alertId))
                .thenReturn(Optional.of(mockAlert()));

        when(linkRepository.existsByIncidentIdAndAlertId(
                incidentId,
                alertId
        )).thenReturn(true);

        IncidentAlertLinkAlreadyExistsException exception =
                assertThrows(
                        IncidentAlertLinkAlreadyExistsException.class,
                        () -> service.linkAlertToIncident(
                                incidentId,
                                alertId
                        )
                );

        assertEquals(
                "Alert is already linked to the incident",
                exception.getMessage()
        );

        verify(linkRepository)
                .existsByIncidentIdAndAlertId(
                        incidentId,
                        alertId
                );

        verify(linkRepository, never())
                .save(any());
    }

    // ============================================================
    // linkAlertToIncident - CONCURRENT DUPLICATE
    // ============================================================

    @Test
    void shouldConvertDuplicateKeyExceptionToAlreadyExistsException() {

        String incidentId = "incident-123";
        String alertId = "alert-123";

        when(incidentRepository.findById(incidentId))
                .thenReturn(Optional.of(mockIncident()));

        when(alertRepository.findById(alertId))
                .thenReturn(Optional.of(mockAlert()));

        when(linkRepository.existsByIncidentIdAndAlertId(
                incidentId,
                alertId
        )).thenReturn(false);

        when(linkRepository.save(any(IncidentAlertLink.class)))
                .thenThrow(new DuplicateKeyException(
                        "Duplicate key"
                ));

        IncidentAlertLinkAlreadyExistsException exception =
                assertThrows(
                        IncidentAlertLinkAlreadyExistsException.class,
                        () -> service.linkAlertToIncident(
                                incidentId,
                                alertId
                        )
                );

        assertEquals(
                "Alert is already linked to the incident",
                exception.getMessage()
        );

        verify(linkRepository)
                .save(any(IncidentAlertLink.class));
    }

    // ============================================================
    // linkAlertToIncident - NULL INCIDENT ID
    // ============================================================

    @Test
    void shouldRejectNullIncidentId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.linkAlertToIncident(
                                null,
                                "alert-123"
                        )
                );

        assertEquals(
                "Incident ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                incidentRepository,
                alertRepository,
                linkRepository
        );
    }

    // ============================================================
    // linkAlertToIncident - BLANK INCIDENT ID
    // ============================================================

    @Test
    void shouldRejectBlankIncidentId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.linkAlertToIncident(
                                "   ",
                                "alert-123"
                        )
                );

        assertEquals(
                "Incident ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                incidentRepository,
                alertRepository,
                linkRepository
        );
    }

    // ============================================================
    // linkAlertToIncident - NULL ALERT ID
    // ============================================================

    @Test
    void shouldRejectNullAlertId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.linkAlertToIncident(
                                "incident-123",
                                null
                        )
                );

        assertEquals(
                "Alert ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                incidentRepository,
                alertRepository,
                linkRepository
        );
    }

    // ============================================================
    // linkAlertToIncident - BLANK ALERT ID
    // ============================================================

    @Test
    void shouldRejectBlankAlertId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.linkAlertToIncident(
                                "incident-123",
                                "   "
                        )
                );

        assertEquals(
                "Alert ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                incidentRepository,
                alertRepository,
                linkRepository
        );
    }

    // ============================================================
    // unlinkAlertFromIncident - SUCCESS
    // ============================================================

    @Test
    void shouldUnlinkAlertFromIncidentSuccessfully() {

        String incidentId = "incident-123";
        String alertId = "alert-123";

        when(incidentRepository.findById(incidentId))
                .thenReturn(Optional.of(mockIncident()));

        when(alertRepository.findById(alertId))
                .thenReturn(Optional.of(mockAlert()));

        when(linkRepository.existsByIncidentIdAndAlertId(
                incidentId,
                alertId
        )).thenReturn(true);

        service.unlinkAlertFromIncident(
                incidentId,
                alertId
        );

        verify(incidentRepository)
                .findById(incidentId);

        verify(alertRepository)
                .findById(alertId);

        verify(linkRepository)
                .existsByIncidentIdAndAlertId(
                        incidentId,
                        alertId
                );

        verify(linkRepository)
                .deleteByIncidentIdAndAlertId(
                        incidentId,
                        alertId
                );
    }

    // ============================================================
    // unlinkAlertFromIncident - INCIDENT NOT FOUND
    // ============================================================

    @Test
    void shouldThrowIncidentNotFoundWhenUnlinkingUnknownIncident() {

        String incidentId = "unknown-incident";
        String alertId = "alert-123";

        when(incidentRepository.findById(incidentId))
                .thenReturn(Optional.empty());

        IncidentNotFoundException exception =
                assertThrows(
                        IncidentNotFoundException.class,
                        () -> service.unlinkAlertFromIncident(
                                incidentId,
                                alertId
                        )
                );

        assertEquals(
                "Incident not found: " + incidentId,
                exception.getMessage()
        );

        verifyNoInteractions(alertRepository);

        verify(linkRepository, never())
                .deleteByIncidentIdAndAlertId(
                        any(),
                        any()
                );
    }

    // ============================================================
    // unlinkAlertFromIncident - ALERT NOT FOUND
    // ============================================================

    @Test
    void shouldThrowAlertNotFoundWhenUnlinkingUnknownAlert() {
        String incidentId = "incident-123";
        String alertId = "alert-999";

        Incident incident = mock(Incident.class);

        when(incidentRepository.findById(incidentId))
                .thenReturn(Optional.of(incident));

        when(alertRepository.findById(alertId))
                .thenReturn(Optional.empty());

        AlertNotFoundException exception = assertThrows(
                AlertNotFoundException.class,
                () -> service.unlinkAlertFromIncident(incidentId, alertId)
        );

        assertEquals("Alert not found: " + alertId, exception.getMessage());

        verify(incidentRepository).findById(incidentId);
        verify(alertRepository).findById(alertId);

        verifyNoInteractions(linkRepository);
    }

    // ============================================================
    // unlinkAlertFromIncident - LINK NOT FOUND
    // ============================================================

    @Test
    void shouldThrowWhenLinkDoesNotExist() {

        String incidentId = "incident-123";
        String alertId = "alert-123";

        when(incidentRepository.findById(incidentId))
                .thenReturn(Optional.of(mockIncident()));

        when(alertRepository.findById(alertId))
                .thenReturn(Optional.of(mockAlert()));

        when(linkRepository.existsByIncidentIdAndAlertId(
                incidentId,
                alertId
        )).thenReturn(false);

        IncidentAlertLinkNotFoundException exception =
                assertThrows(
                        IncidentAlertLinkNotFoundException.class,
                        () -> service.unlinkAlertFromIncident(
                                incidentId,
                                alertId
                        )
                );

        assertEquals(
                "Alert is not linked to the incident",
                exception.getMessage()
        );

        verify(linkRepository, never())
                .deleteByIncidentIdAndAlertId(
                        any(),
                        any()
                );
    }

    // ============================================================
    // unlinkAlertFromIncident - NULL INCIDENT ID
    // ============================================================

    @Test
    void shouldRejectNullIncidentIdWhenUnlinking() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.unlinkAlertFromIncident(
                                null,
                                "alert-123"
                        )
                );

        assertEquals(
                "Incident ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                incidentRepository,
                alertRepository,
                linkRepository
        );
    }

    // ============================================================
    // unlinkAlertFromIncident - NULL ALERT ID
    // ============================================================

    @Test
    void shouldRejectNullAlertIdWhenUnlinking() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.unlinkAlertFromIncident(
                                "incident-123",
                                null
                        )
                );

        assertEquals(
                "Alert ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                incidentRepository,
                alertRepository,
                linkRepository
        );
    }

    // ============================================================
    // getAlertsForIncident - SUCCESS
    // ============================================================

    @Test
    void shouldGetAlertsForIncidentSuccessfully() {

        String incidentId = "incident-123";

        IncidentAlertLink link1 =
                new IncidentAlertLink(
                        incidentId,
                        "alert-1"
                );

        IncidentAlertLink link2 =
                new IncidentAlertLink(
                        incidentId,
                        "alert-2"
                );

        when(incidentRepository.findById(incidentId))
                .thenReturn(Optional.of(mockIncident()));

        when(linkRepository.findByIncidentIdOrderByCreatedAtAsc(
                incidentId
        )).thenReturn(List.of(link1, link2));

        List<IncidentAlertLinkResponse> result =
                service.getAlertsForIncident(incidentId);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(
                incidentId,
                result.get(0).incidentId()
        );

        assertEquals(
                "alert-1",
                result.get(0).alertId()
        );

        assertEquals(
                "alert-2",
                result.get(1).alertId()
        );

        verify(incidentRepository)
                .findById(incidentId);

        verify(linkRepository)
                .findByIncidentIdOrderByCreatedAtAsc(
                        incidentId
                );
    }

    // ============================================================
    // getAlertsForIncident - EMPTY RESULT
    // ============================================================

    @Test
    void shouldReturnEmptyListWhenIncidentHasNoAlerts() {

        String incidentId = "incident-123";

        when(incidentRepository.findById(incidentId))
                .thenReturn(Optional.of(mockIncident()));

        when(linkRepository.findByIncidentIdOrderByCreatedAtAsc(
                incidentId
        )).thenReturn(List.of());

        List<IncidentAlertLinkResponse> result =
                service.getAlertsForIncident(incidentId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(linkRepository)
                .findByIncidentIdOrderByCreatedAtAsc(
                        incidentId
                );
    }

    // ============================================================
    // getAlertsForIncident - INCIDENT NOT FOUND
    // ============================================================

    @Test
    void shouldThrowIncidentNotFoundWhenGettingAlertsForUnknownIncident() {

        String incidentId = "unknown-incident";

        when(incidentRepository.findById(incidentId))
                .thenReturn(Optional.empty());

        IncidentNotFoundException exception =
                assertThrows(
                        IncidentNotFoundException.class,
                        () -> service.getAlertsForIncident(
                                incidentId
                        )
                );

        assertEquals(
                "Incident not found: " + incidentId,
                exception.getMessage()
        );

        verify(linkRepository, never())
                .findByIncidentIdOrderByCreatedAtAsc(any());
    }

    // ============================================================
    // getAlertsForIncident - INVALID ID
    // ============================================================

    @Test
    void shouldRejectBlankIncidentIdWhenGettingAlerts() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getAlertsForIncident(
                                " "
                        )
                );

        assertEquals(
                "Incident ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                incidentRepository,
                alertRepository,
                linkRepository
        );
    }

    // ============================================================
    // getIncidentsForAlert - SUCCESS
    // ============================================================

    @Test
    void shouldGetIncidentsForAlertSuccessfully() {

        String alertId = "alert-123";

        IncidentAlertLink link1 =
                new IncidentAlertLink(
                        "incident-1",
                        alertId
                );

        IncidentAlertLink link2 =
                new IncidentAlertLink(
                        "incident-2",
                        alertId
                );

        when(alertRepository.findById(alertId))
                .thenReturn(Optional.of(mockAlert()));

        when(linkRepository.findByAlertIdOrderByCreatedAtAsc(
                alertId
        )).thenReturn(List.of(link1, link2));

        List<IncidentAlertLinkResponse> result =
                service.getIncidentsForAlert(alertId);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(
                "incident-1",
                result.get(0).incidentId()
        );

        assertEquals(
                alertId,
                result.get(0).alertId()
        );

        assertEquals(
                "incident-2",
                result.get(1).incidentId()
        );

        verify(alertRepository)
                .findById(alertId);

        verify(linkRepository)
                .findByAlertIdOrderByCreatedAtAsc(
                        alertId
                );
    }

    // ============================================================
    // getIncidentsForAlert - EMPTY RESULT
    // ============================================================

    @Test
    void shouldReturnEmptyListWhenAlertHasNoIncidents() {

        String alertId = "alert-123";

        when(alertRepository.findById(alertId))
                .thenReturn(Optional.of(mockAlert()));

        when(linkRepository.findByAlertIdOrderByCreatedAtAsc(
                alertId
        )).thenReturn(List.of());

        List<IncidentAlertLinkResponse> result =
                service.getIncidentsForAlert(alertId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(linkRepository)
                .findByAlertIdOrderByCreatedAtAsc(
                        alertId
                );
    }

    // ============================================================
    // getIncidentsForAlert - ALERT NOT FOUND
    // ============================================================

    @Test
    void shouldThrowAlertNotFoundWhenGettingIncidentsForUnknownAlert() {
        String alertId = "alert-999";

        when(alertRepository.findById(alertId))
                .thenReturn(Optional.empty());

        AlertNotFoundException exception = assertThrows(
                AlertNotFoundException.class,
                () -> service.getIncidentsForAlert(alertId)
        );

        assertEquals("Alert not found: " + alertId, exception.getMessage());

        verify(alertRepository).findById(alertId);

        verifyNoInteractions(incidentRepository, linkRepository);
    }

    // ============================================================
    // getIncidentsForAlert - INVALID ID
    // ============================================================

    @Test
    void shouldRejectBlankAlertIdWhenGettingIncidents() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getIncidentsForAlert(
                                " "
                        )
                );

        assertEquals(
                "Alert ID is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                incidentRepository,
                alertRepository,
                linkRepository
        );
    }

    // ============================================================
    // Helper methods
    // ============================================================

    private com.gckavach.gckavachapp.incident.domain.Incident mockIncident() {
        return mock(
                com.gckavach.gckavachapp.incident.domain.Incident.class
        );
    }

    private Alert mockAlert() {
        return mock(Alert.class);
    }
}