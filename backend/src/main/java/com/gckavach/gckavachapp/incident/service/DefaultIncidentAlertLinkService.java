package com.gckavach.gckavachapp.incident.service;

import com.gckavach.gckavachapp.alert.domain.Alert;
import com.gckavach.gckavachapp.alert.repository.AlertRepository;
import com.gckavach.gckavachapp.common.exception.AlertNotFoundException;
import com.gckavach.gckavachapp.common.exception.IncidentAlertLinkAlreadyExistsException;
import com.gckavach.gckavachapp.common.exception.IncidentAlertLinkNotFoundException;
import com.gckavach.gckavachapp.common.exception.IncidentNotFoundException;
import com.gckavach.gckavachapp.incident.api.IncidentAlertLinkResponse;

import com.gckavach.gckavachapp.incident.domain.IncidentAlertLink;
import com.gckavach.gckavachapp.incident.repository.IncidentAlertLinkRepository;
import com.gckavach.gckavachapp.incident.repository.IncidentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultIncidentAlertLinkService
        implements IncidentAlertLinkService {

    Logger log = LoggerFactory.getLogger(DefaultIncidentAlertLinkService.class);
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final IncidentAlertLinkRepository linkRepository;

    public DefaultIncidentAlertLinkService(
            IncidentRepository incidentRepository,
            AlertRepository alertRepository,
            IncidentAlertLinkRepository linkRepository) {

        this.incidentRepository = incidentRepository;
        this.alertRepository = alertRepository;
        this.linkRepository = linkRepository;

        log.info("DefaultIncidentAlertLinkService::initialized");
    }

    @Override
    public IncidentAlertLinkResponse linkAlertToIncident(
            String incidentId,
            String alertId) {

        validateIdentifiers(incidentId, alertId);

        log.info(
                "Linking alert to incident: incidentId={}, alertId={}",
                incidentId,
                alertId
        );

        getRequiredIncident(incidentId);
        getRequiredAlert(alertId);

        if (linkRepository.existsByIncidentIdAndAlertId(
                incidentId,
                alertId)) {

            log.warn(
                    "Incident-alert link already exists: incidentId={}, alertId={}",
                    incidentId,
                    alertId
            );

            throw new IncidentAlertLinkAlreadyExistsException(
                    "Alert is already linked to the incident"
            );
        }

        IncidentAlertLink link =
                new IncidentAlertLink(
                        incidentId,
                        alertId
                );

        try {

            IncidentAlertLink savedLink =
                    linkRepository.save(link);

            log.info(
                    "Alert linked successfully: incidentId={}, alertId={}, linkId={}",
                    incidentId,
                    alertId,
                    savedLink.getId()
            );

            return IncidentAlertLinkResponse.from(savedLink);

        } catch (DuplicateKeyException exception) {

            /*
             * Two concurrent requests may both pass
             * existsByIncidentIdAndAlertId().
             *
             * The MongoDB unique compound index is the
             * final concurrency protection.
             */

            log.warn(
                    "Concurrent duplicate incident-alert link rejected: incidentId={}, alertId={}",
                    incidentId,
                    alertId
            );

            throw new IncidentAlertLinkAlreadyExistsException(
                    "Alert is already linked to the incident"
            );
        }
    }

    @Override
    public void unlinkAlertFromIncident(
            String incidentId,
            String alertId) {

        validateIdentifiers(incidentId, alertId);

        log.info(
                "Unlinking alert from incident: incidentId={}, alertId={}",
                incidentId,
                alertId
        );

        getRequiredIncident(incidentId);
        getRequiredAlert(alertId);

        if (!linkRepository.existsByIncidentIdAndAlertId(
                incidentId,
                alertId)) {

            log.warn(
                    "Incident-alert link not found: incidentId={}, alertId={}",
                    incidentId,
                    alertId
            );

            throw new IncidentAlertLinkNotFoundException(
                    "Alert is not linked to the incident"
            );
        }

        linkRepository.deleteByIncidentIdAndAlertId(
                incidentId,
                alertId
        );

        log.info(
                "Alert unlinked successfully: incidentId={}, alertId={}",
                incidentId,
                alertId
        );
    }

    @Override
    public List<IncidentAlertLinkResponse> getAlertsForIncident(
            String incidentId) {

        validateIncidentId(incidentId);

        log.debug(
                "Fetching alerts linked to incident: incidentId={}",
                incidentId
        );

        getRequiredIncident(incidentId);

        List<IncidentAlertLink> links =
                linkRepository.findByIncidentIdOrderByCreatedAtAsc(
                        incidentId
                );

        log.debug(
                "Incident alert links retrieved: incidentId={}, count={}",
                incidentId,
                links.size()
        );

        return links.stream()
                .map(IncidentAlertLinkResponse::from)
                .toList();
    }

    @Override
    public List<IncidentAlertLinkResponse> getIncidentsForAlert(
            String alertId) {

        validateAlertId(alertId);

        log.debug(
                "Fetching incidents linked to alert: alertId={}",
                alertId
        );

        getRequiredAlert(alertId);

        List<IncidentAlertLink> links =
                linkRepository.findByAlertIdOrderByCreatedAtAsc(
                        alertId
                );

        log.debug(
                "Alert incident links retrieved: alertId={}, count={}",
                alertId,
                links.size()
        );

        return links.stream()
                .map(IncidentAlertLinkResponse::from)
                .toList();
    }

    private void getRequiredIncident(String incidentId) {

        incidentRepository
                .findById(incidentId)
                .orElseThrow(() -> {

                    log.warn(
                            "Incident not found while processing alert link: incidentId={}",
                            incidentId
                    );

                    return new IncidentNotFoundException(
                            "Incident not found: " + incidentId
                    );
                });
    }

    private Alert getRequiredAlert(String alertId) {

        return alertRepository
                .findById(alertId)
                .orElseThrow(() -> {

                    log.warn(
                            "Alert not found while processing incident link: alertId={}",
                            alertId
                    );

                    return new AlertNotFoundException(
                            "Alert not found: " + alertId
                    );
                });
    }

    private void validateIdentifiers(
            String incidentId,
            String alertId) {

        validateIncidentId(incidentId);
        validateAlertId(alertId);
    }

    private void validateIncidentId(String incidentId) {

        if (incidentId == null || incidentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Incident ID is required"
            );
        }
    }

    private void validateAlertId(String alertId) {

        if (alertId == null || alertId.isBlank()) {
            throw new IllegalArgumentException(
                    "Alert ID is required"
            );
        }
    }
}