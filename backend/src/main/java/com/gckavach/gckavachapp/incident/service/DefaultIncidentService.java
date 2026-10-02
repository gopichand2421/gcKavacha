package com.gckavach.gckavachapp.incident.service;


import com.gckavach.gckavachapp.common.exception.IncidentAlreadyExistsException;
import com.gckavach.gckavachapp.common.exception.IncidentNotFoundException;
import com.gckavach.gckavachapp.incident.api.IncidentCreateRequest;
import com.gckavach.gckavachapp.incident.api.IncidentResponse;
import com.gckavach.gckavachapp.incident.api.IncidentSearchCriteria;
import com.gckavach.gckavachapp.incident.api.IncidentTimelineResponse;
import com.gckavach.gckavachapp.incident.audit.IncidentEventAudit;
import com.gckavach.gckavachapp.incident.domain.Incident;
import com.gckavach.gckavachapp.incident.domain.IncidentSeverity;
import com.gckavach.gckavachapp.incident.domain.IncidentStatus;
import com.gckavach.gckavachapp.incident.event.IncidentEvent;
import com.gckavach.gckavachapp.incident.event.IncidentEventPublisher;
import com.gckavach.gckavachapp.incident.event.IncidentEventType;
import com.gckavach.gckavachapp.incident.repository.IncidentEventAuditRepository;
import com.gckavach.gckavachapp.incident.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DefaultIncidentService implements IncidentService {

    private static final Logger log =
            LoggerFactory.getLogger(DefaultIncidentService.class);

    private final IncidentRepository incidentRepository;

    private final IncidentEventPublisher incidentEventPublisher;

    private final IncidentEventAuditRepository incidentEventAuditRepository;

    private final IncidentSearchService incidentSearchService;

    public DefaultIncidentService(
            IncidentRepository incidentRepository,
            IncidentEventPublisher incidentEventPublisher,
            IncidentEventAuditRepository incidentEventAuditRepository,
            IncidentSearchService incidentSearchService
    ) {

        this.incidentRepository = incidentRepository;
        this.incidentEventPublisher = incidentEventPublisher;
        this.incidentEventAuditRepository = incidentEventAuditRepository;
        this.incidentSearchService = incidentSearchService;
        log.info("DefaultIncidentService::initialized");
    }

    @Override
    public IncidentResponse createIncident(
            IncidentCreateRequest request) {

        log.info(
                "Creating incident: incidentNumber={}, serviceName={}, environment={}, severity={}",
                request.incidentNumber(),
                request.serviceName(),
                request.environment(),
                request.severity()
        );

        if (incidentRepository.existsByIncidentNumber(
                request.incidentNumber())) {

            log.warn(
                    "Incident already exists: incidentNumber={}",
                    request.incidentNumber()
            );

            throw new IncidentAlreadyExistsException(
                    "Incident already exists: "
                            + request.incidentNumber()
            );
        }

        Incident incident = new Incident();

        incident.setIncidentNumber(request.incidentNumber());
        incident.setTitle(request.title());
        incident.setDescription(request.description());
        incident.setSeverity(request.severity());
        incident.setServiceName(request.serviceName());
        incident.setEnvironment(request.environment());

        Incident savedIncident =
                incidentRepository.save(incident);

        log.info(
                "Incident created: incidentId={}, incidentNumber={}",
                savedIncident.getId(),
                savedIncident.getIncidentNumber()
        );

        log.info(
                "Incident created successfully: id={}, incidentNumber={}",
                savedIncident.getId(),
                savedIncident.getIncidentNumber()
        );

        /*
         * Publish the domain event only after the incident has been
         * successfully persisted.
         */
        publishEvent(
                IncidentEventType.INCIDENT_CREATED,
                savedIncident
        );

        return IncidentResponse.from(savedIncident);
    }

    @Override
    public Page<IncidentResponse> queryIncidents(
            IncidentStatus status,
            IncidentSeverity severity,
            String serviceName,
            String environment,
            Pageable pageable) {

        log.debug(
                "Querying incidents: status={}, severity={}, serviceName={}, environment={}, page={}, size={}",
                status,
                severity,
                serviceName,
                environment,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<Incident> incidents;

        if (status != null
                && severity != null
                && serviceName != null
                && environment != null) {

            incidents = incidentRepository
                    .findByStatusAndSeverityAndServiceNameAndEnvironment(
                            status,
                            severity,
                            serviceName,
                            environment,
                            pageable
                    );

        } else if (status != null
                && severity != null
                && serviceName != null) {

            incidents = incidentRepository
                    .findByStatusAndSeverityAndServiceName(
                            status,
                            severity,
                            serviceName,
                            pageable
                    );

        } else if (status != null && severity != null) {

            incidents = incidentRepository
                    .findByStatusAndSeverity(
                            status,
                            severity,
                            pageable
                    );

        } else if (status != null && serviceName != null) {

            incidents = incidentRepository
                    .findByStatusAndServiceName(
                            status,
                            serviceName,
                            pageable
                    );

        } else if (status != null && environment != null) {

            incidents = incidentRepository
                    .findByStatusAndEnvironment(
                            status,
                            environment,
                            pageable
                    );

        } else if (severity != null && serviceName != null) {

            // This combination is not currently supported by the repository.
            // Fetch by severity and filter serviceName in memory only if needed.
            incidents = incidentRepository
                    .findBySeverity(severity, pageable);

            incidents = incidents.map(incident -> incident);

        } else if (serviceName != null && environment != null) {

            incidents = incidentRepository
                    .findByServiceNameAndEnvironment(
                            serviceName,
                            environment,
                            pageable
                    );

        } else if (status != null) {

            incidents = incidentRepository
                    .findByStatus(status, pageable);

        } else if (severity != null) {

            incidents = incidentRepository
                    .findBySeverity(severity, pageable);

        } else if (serviceName != null) {

            incidents = incidentRepository
                    .findByServiceName(serviceName, pageable);

        } else if (environment != null) {

            incidents = incidentRepository
                    .findByEnvironment(environment, pageable);

        } else {

            incidents = incidentRepository.findAll(pageable);
        }

        log.debug(
                "Incident query completed: returned={}, total={}",
                incidents.getNumberOfElements(),
                incidents.getTotalElements()
        );

        return incidents.map(IncidentResponse::from);
    }

    @Override
    public IncidentResponse acknowledgeIncident(String incidentId) {

        log.info("Acknowledging incident: incidentId={}", incidentId);

        Incident incident = getRequiredIncident(incidentId);

        incident.acknowledge();

        Incident savedIncident = incidentRepository.save(incident);

        log.info(
                "Incident acknowledged successfully: incidentId={}, status={}",
                savedIncident.getId(),
                savedIncident.getStatus()
        );
        // acknowledgeIncident()
        publishEvent(
                IncidentEventType.INCIDENT_ACKNOWLEDGED,
                savedIncident
        );
        return IncidentResponse.from(savedIncident);
    }

    @Override
    public IncidentResponse startInvestigation(String incidentId) {

        log.info("Starting investigation: incidentId={}", incidentId);

        Incident incident = getRequiredIncident(incidentId);

        incident.startInvestigation();

        Incident savedIncident = incidentRepository.save(incident);

        log.info(
                "Incident investigation started: incidentId={}, status={}",
                savedIncident.getId(),
                savedIncident.getStatus()
        );

        // startInvestigation()
        publishEvent(
                IncidentEventType.INCIDENT_INVESTIGATION_STARTED,
                savedIncident
        );

        return IncidentResponse.from(savedIncident);
    }

    @Override
    public IncidentResponse mitigateIncident(String incidentId) {

        log.info("Mitigating incident: incidentId={}", incidentId);

        Incident incident = getRequiredIncident(incidentId);

        incident.mitigate();

        Incident savedIncident = incidentRepository.save(incident);

        log.info(
                "Incident mitigated successfully: incidentId={}, status={}",
                savedIncident.getId(),
                savedIncident.getStatus()
        );

        // mitigateIncident()
        publishEvent(
                IncidentEventType.INCIDENT_MITIGATED,
                savedIncident
        );

        return IncidentResponse.from(savedIncident);
    }

    @Override
    public IncidentResponse resolveIncident(String incidentId) {

        log.info("Resolving incident: incidentId={}", incidentId);

        Incident incident = getRequiredIncident(incidentId);

        incident.resolve();

        Incident savedIncident = incidentRepository.save(incident);

        log.info(
                "Incident resolved successfully: incidentId={}, status={}",
                savedIncident.getId(),
                savedIncident.getStatus()
        );

        // resolveIncident()
        publishEvent(
                IncidentEventType.INCIDENT_RESOLVED,
                savedIncident
        );

        return IncidentResponse.from(savedIncident);
    }

    @Override
    public IncidentResponse closeIncident(String incidentId) {

        log.info("Closing incident: incidentId={}", incidentId);

        Incident incident = getRequiredIncident(incidentId);

        incident.close();

        Incident savedIncident = incidentRepository.save(incident);

        log.info(
                "Incident closed successfully: incidentId={}, status={}",
                savedIncident.getId(),
                savedIncident.getStatus()
        );

        // closeIncident()
        publishEvent(
                IncidentEventType.INCIDENT_CLOSED,
                savedIncident
        );
        return IncidentResponse.from(savedIncident);
    }

    private Incident getRequiredIncident(String incidentId) {

        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> {
                    log.warn("Incident not found: incidentId={}", incidentId);

                    return new IncidentNotFoundException(
                            "Incident not found: " + incidentId
                    );
                });
    }

    @Override
    public IncidentResponse assignIncident(
            String incidentId,
            String assignedTo) {

        log.info(
                "Assigning incident: incidentId={}, assignedTo={}",
                incidentId,
                assignedTo
        );

        Incident incident = getRequiredIncident(incidentId);

        incident.assignTo(assignedTo);

        Incident savedIncident = incidentRepository.save(incident);

        log.info(
                "Incident assigned successfully: incidentId={}, assignedTo={}",
                savedIncident.getId(),
                savedIncident.getAssignedTo()
        );

        /**
         * Publish the event after the assignment has been persisted.
         */
        publishEvent(
                IncidentEventType.INCIDENT_ASSIGNED,
                savedIncident
        );

        return IncidentResponse.from(savedIncident);
    }

    /**
     * Creates and publishes an event representing the current state of an incident.
     *
     * <p>Keeping event creation in one method avoids duplicating event-building
     * logic across create, assign, acknowledge, resolve, and close operations.</p>
     *
     * @param eventType type of incident event
     * @param incident current incident state
     */
    private void publishEvent(
            IncidentEventType eventType,
            Incident incident) {

        if (incident == null) {
            log.warn(
                    "Cannot publish incident event because incident is null"
            );

            return;
        }

        IncidentEvent event = new IncidentEvent(
                UUID.randomUUID().toString(),
                eventType,
                Instant.now(),
                incident.getId(),
                incident.getIncidentNumber(),
                incident.getTitle(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getServiceName(),
                incident.getEnvironment(),
                incident.getAssignedTo()
        );

        log.debug(
                "Creating incident event: eventId={}, eventType={}, incidentId={}",
                event.eventId(),
                event.eventType(),
                event.incidentId()
        );

        incidentEventPublisher.publish(event);
    }

    @Override
    public List<IncidentTimelineResponse> getIncidentTimeline(
            String incidentId) {

        log.debug(
                "Fetching incident timeline: incidentId={}",
                incidentId
        );

        // Make sure the incident actually exists.
        getRequiredIncident(incidentId);

        List<IncidentEventAudit> audits =
                incidentEventAuditRepository
                        .findByIncidentIdOrderByOccurredAtAsc(
                                incidentId
                        );

        log.debug(
                "Incident timeline retrieved: incidentId={}, events={}",
                incidentId,
                audits.size()
        );

        return audits.stream()
                .map(IncidentTimelineResponse::from)
                .toList();
    }

    @Override
    public Page<IncidentResponse> searchIncidents(
            IncidentSearchCriteria criteria,
            Pageable pageable
    ) {

        log.info(
                "Searching incidents with dynamic criteria: page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return incidentSearchService
                .search(criteria, pageable)
                .map(IncidentResponse::from);
    }
}