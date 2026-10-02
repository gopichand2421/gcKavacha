package com.gckavach.gckavachapp.incident.controller;

import com.gckavach.gckavachapp.incident.api.*;
import com.gckavach.gckavachapp.incident.domain.Incident;
import com.gckavach.gckavachapp.incident.domain.IncidentSeverity;
import com.gckavach.gckavachapp.incident.domain.IncidentStatus;
import com.gckavach.gckavachapp.incident.event.IncidentEvent;
import com.gckavach.gckavachapp.incident.event.IncidentEventPublisher;
import com.gckavach.gckavachapp.incident.event.IncidentEventType;
import com.gckavach.gckavachapp.incident.repository.IncidentRepository;
import com.gckavach.gckavachapp.incident.service.IncidentAlertLinkService;
import com.gckavach.gckavachapp.incident.service.IncidentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private static final Logger log =
            LoggerFactory.getLogger(IncidentController.class);

    private final IncidentRepository incidentRepository;
    private final IncidentService incidentService;
    private final IncidentEventPublisher incidentEventPublisher;
    private final IncidentAlertLinkService incidentAlertLinkService;

    public IncidentController(
            IncidentRepository incidentRepository,
            IncidentService incidentService,
            IncidentEventPublisher incidentEventPublisher,
            IncidentAlertLinkService incidentAlertLinkService
    ) {

        this.incidentRepository = incidentRepository;
        this.incidentService = incidentService;
        this.incidentEventPublisher = incidentEventPublisher;
        this.incidentAlertLinkService = incidentAlertLinkService;
        log.info("IncidentController::initialized");
    }

    // =========================================================================
    // CREATE INCIDENT
    // =========================================================================

    /**
     * Creates a new incident.
     *
     * POST /api/incidents
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<IncidentResponse> createIncident(
            @Valid @RequestBody IncidentCreateRequest request) {

        log.info(
                "Creating incident: incidentNumber={}, serviceName={}, environment={}, severity={}",
                request.incidentNumber(),
                request.serviceName(),
                request.environment(),
                request.severity()
        );

        IncidentResponse response =
                incidentService.createIncident(request);

        log.info(
                "Incident created successfully: incidentId={}, incidentNumber={}",
                response.id(),
                response.incidentNumber()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================================================
    // GET INCIDENT
    // =========================================================================

    /**
     * Gets an incident by ID.
     *
     * GET /api/incidents/{incidentId}
     */
    @GetMapping("/{incidentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<IncidentResponse> getIncident(
            @PathVariable String incidentId) {

        log.debug(
                "Fetching incident: incidentId={}",
                incidentId
        );

        Incident incident = getRequiredIncident(incidentId);

        return ResponseEntity.ok(
                IncidentResponse.from(incident)
        );
    }

    // =========================================================================
    // QUERY INCIDENTS
    // =========================================================================

    /**
     * Queries incidents using optional filters.
     *
     * GET /api/incidents
     *
     * Supported filters:
     *
     * status
     * severity
     * serviceName
     * environment
     * page
     * size
     * sort
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<IncidentResponse>> queryIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IncidentSeverity severity,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String environment,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        page = Math.max(page, 0);
        size = Math.min(Math.max(size, 1), 100);

        Pageable pageable = createPageable(
                page,
                size,
                sort
        );

        Page<IncidentResponse> response =
                incidentService.queryIncidents(
                        status,
                        severity,
                        serviceName,
                        environment,
                        pageable
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // ACKNOWLEDGE
    // =========================================================================

    /**
     * Acknowledges an incident.
     *
     * PATCH /api/incidents/{incidentId}/acknowledge
     */
    @PatchMapping("/{incidentId}/acknowledge")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<IncidentResponse> acknowledgeIncident(
            @PathVariable String incidentId) {

        log.info(
                "Acknowledging incident: incidentId={}",
                incidentId
        );

        Incident incident = getRequiredIncident(incidentId);

        // Apply the domain state transition.
        incident.acknowledge();

        // Persist the new incident state first.
        Incident savedIncident = incidentRepository.save(incident);

        log.info(
                "Incident acknowledged successfully: incidentId={}, status={}",
                savedIncident.getId(),
                savedIncident.getStatus()
        );

        // Publish an event after the state has been persisted.
        publishEvent(
                IncidentEventType.INCIDENT_ACKNOWLEDGED,
                savedIncident
        );


        return ResponseEntity.ok(
                IncidentResponse.from(savedIncident)
        );
    }

    // =========================================================================
    // START INVESTIGATION
    // =========================================================================

    /**
     * Starts investigation of an incident.
     *
     * PATCH /api/incidents/{incidentId}/investigate
     */
    @PatchMapping("/{incidentId}/investigate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<IncidentResponse> startInvestigation(
            @PathVariable String incidentId) {

        log.info(
                "Starting incident investigation: incidentId={}",
                incidentId
        );

        Incident incident =
                getRequiredIncident(incidentId);

        incident.startInvestigation();

        Incident savedIncident =
                incidentRepository.save(incident);

        log.info(
                "Incident investigation started: incidentId={}",
                incidentId
        );

        return ResponseEntity.ok(
                IncidentResponse.from(savedIncident)
        );
    }

    // =========================================================================
    // MITIGATE
    // =========================================================================

    /**
     * Marks an incident as mitigated.
     *
     * PATCH /api/incidents/{incidentId}/mitigate
     */
    @PatchMapping("/{incidentId}/mitigate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<IncidentResponse> mitigateIncident(
            @PathVariable String incidentId) {

        log.info(
                "Mitigating incident: incidentId={}",
                incidentId
        );

        Incident incident =
                getRequiredIncident(incidentId);

        incident.mitigate();

        Incident savedIncident =
                incidentRepository.save(incident);

        log.info(
                "Incident mitigated: incidentId={}",
                incidentId
        );

        return ResponseEntity.ok(
                IncidentResponse.from(savedIncident)
        );
    }

    // =========================================================================
    // RESOLVE
    // =========================================================================

    /**
     * Resolves an incident.
     *
     * PATCH /api/incidents/{incidentId}/resolve
     */
    @PatchMapping("/{incidentId}/resolve")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<IncidentResponse> resolveIncident(
            @PathVariable String incidentId) {

        log.info(
                "Resolving incident: incidentId={}",
                incidentId
        );

        Incident incident =
                getRequiredIncident(incidentId);

        incident.resolve();

        Incident savedIncident =
                incidentRepository.save(incident);

        log.info(
                "Incident resolved: incidentId={}",
                incidentId
        );

        return ResponseEntity.ok(
                IncidentResponse.from(savedIncident)
        );
    }

    // =========================================================================
    // CLOSE
    // =========================================================================

    /**
     * Closes a resolved incident.
     *
     * PATCH /api/incidents/{incidentId}/close
     */
    @PatchMapping("/{incidentId}/close")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<IncidentResponse> closeIncident(
            @PathVariable String incidentId) {

        log.info(
                "Closing incident: incidentId={}",
                incidentId
        );

        Incident incident =
                getRequiredIncident(incidentId);

        incident.close();

        Incident savedIncident =
                incidentRepository.save(incident);

        log.info(
                "Incident closed: incidentId={}",
                incidentId
        );

        return ResponseEntity.ok(
                IncidentResponse.from(savedIncident)
        );
    }

    // =========================================================================
    // ASSIGN
    // =========================================================================

    /**
     * Assigns an incident to an engineer.
     *
     * PATCH /api/incidents/{incidentId}/assign
     */
    @PatchMapping("/{incidentId}/assign")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<IncidentResponse> assignIncident(
            @PathVariable String incidentId,
            @Valid @RequestBody AssignIncidentRequest request) {

        log.info(
                "Assign incident request received: incidentId={}",
                incidentId
        );

        IncidentResponse response = incidentService.assignIncident(
                incidentId,
                request.assignedTo()
        );

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    /**
     * Deletes an incident.
     *
     * DELETE /api/incidents/{incidentId}
     */
    @DeleteMapping("/{incidentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteIncident(
            @PathVariable String incidentId) {

        log.info(
                "Deleting incident: incidentId={}",
                incidentId
        );

        Incident incident =
                getRequiredIncident(incidentId);

        incidentRepository.delete(incident);

        log.info(
                "Incident deleted: incidentId={}",
                incidentId
        );

        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // PRIVATE METHODS
    // =========================================================================

    /**
     * Retrieves an incident or throws a 404-compatible exception.
     */
    private Incident getRequiredIncident(
            String incidentId) {

        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> {

                    log.warn(
                            "Incident not found: incidentId={}",
                            incidentId
                    );

                    return new IncidentNotFoundException(
                            "Incident not found: " + incidentId
                    );
                });
    }

    /**
     * Creates pageable configuration from the request parameters.
     *
     * Expected format:
     *
     * createdAt,desc
     * createdAt,asc
     * severity,desc
     */
    private Pageable createPageable(
            int page,
            int size,
            String sort) {

        String[] sortParts =
                sort.split(",", 2);

        String property =
                sortParts[0].trim();

        if (property.isBlank()) {
            property = "createdAt";
        }

        Sort.Direction direction =
                Sort.Direction.ASC;

        if (sortParts.length > 1
                && "desc".equalsIgnoreCase(
                sortParts[1].trim())) {

            direction = Sort.Direction.DESC;
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(direction, property)
        );
    }

    // =========================================================================
    // REQUEST DTO
    // =========================================================================

    /**
     * Request body for assigning an incident.
     */
    public record AssignIncidentRequest(

            @jakarta.validation.constraints.NotBlank(
                    message = "assignedTo is required"
            )
            String assignedTo
    ) {
    }

    // =========================================================================
    // EXCEPTION
    // =========================================================================

    /**
     * Thrown when an incident cannot be found.
     *
     * This exception should be handled by the application's
     * global exception handler and converted into HTTP 404.
     */
    public static class IncidentNotFoundException
            extends RuntimeException {

        public IncidentNotFoundException(
                String message) {

            super(message);
        }
    }

    /**
     * Creates and publishes an incident domain event.
     *
     * @param eventType type of incident event
     * @param incident current incident state
     */
    private void publishEvent(
            IncidentEventType eventType,
            Incident incident) {

        if (incident == null) {
            log.warn("Cannot publish incident event because incident is null");
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
                "Publishing incident event: eventId={}, type={}, incidentId={}",
                event.eventId(),
                event.eventType(),
                event.incidentId()
        );

        incidentEventPublisher.publish(event);
    }

    /**
     * Returns the chronological event history of an incident.
     *
     * @param incidentId incident identifier
     * @return incident timeline
     */
    @GetMapping("/{incidentId}/timeline")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<IncidentTimelineResponse>> getIncidentTimeline(
            @PathVariable String incidentId) {

        log.info(
                "Fetching incident timeline: incidentId={}",
                incidentId
        );

        List<IncidentTimelineResponse> timeline =
                incidentService.getIncidentTimeline(incidentId);

        return ResponseEntity.ok(timeline);
    }

    @PostMapping("/{incidentId}/alerts/{alertId}")
    public ResponseEntity<IncidentAlertLinkResponse> linkAlert(
            @PathVariable String incidentId,
            @PathVariable String alertId) {

        log.info(
                "POST incident-alert link: incidentId={}, alertId={}",
                incidentId,
                alertId
        );

        IncidentAlertLinkResponse response =
                incidentAlertLinkService.linkAlertToIncident(
                        incidentId,
                        alertId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{incidentId}/alerts/{alertId}")
    public ResponseEntity<Void> unlinkAlert(
            @PathVariable String incidentId,
            @PathVariable String alertId) {

        log.info(
                "DELETE incident-alert link: incidentId={}, alertId={}",
                incidentId,
                alertId
        );

        incidentAlertLinkService.unlinkAlertFromIncident(
                incidentId,
                alertId
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{incidentId}/alerts")
    public ResponseEntity<List<IncidentAlertLinkResponse>> getIncidentAlerts(
            @PathVariable String incidentId) {

        log.debug(
                "GET incident alerts: incidentId={}",
                incidentId
        );

        return ResponseEntity.ok(
                incidentAlertLinkService.getAlertsForIncident(
                        incidentId
                )
        );
    }

    @GetMapping("/{alertId}/incidents")
    public ResponseEntity<List<IncidentAlertLinkResponse>> getAlertIncidents(
            @PathVariable String alertId) {

        log.debug(
                "GET alert incidents: alertId={}",
                alertId
        );

        return ResponseEntity.ok(
                incidentAlertLinkService.getIncidentsForAlert(
                        alertId
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<IncidentResponse>> searchIncidents(
            @RequestParam(required = false)
            IncidentStatus status,

            @RequestParam(required = false)
            IncidentSeverity severity,

            @RequestParam(required = false)
            String serviceName,

            @RequestParam(required = false)
            String environment,

            @RequestParam(required = false)
            String incidentNumber,

            @RequestParam(required = false)
            String title,

            @RequestParam(required = false)
            String assignedTo,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        log.debug(
                "Incident search request received: page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        IncidentSearchCriteria criteria =
                new IncidentSearchCriteria(
                        status,
                        severity,
                        serviceName,
                        environment,
                        incidentNumber,
                        title,
                        assignedTo
                );

        Page<IncidentResponse> response =
                incidentService.searchIncidents(
                        criteria,
                        pageable
                );

        return ResponseEntity.ok(response);
    }

}
