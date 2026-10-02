package com.gckavach.gckavachapp.incident.service;

import com.gckavach.gckavachapp.incident.api.IncidentCreateRequest;
import com.gckavach.gckavachapp.incident.api.IncidentResponse;
import com.gckavach.gckavachapp.incident.api.IncidentSearchCriteria;
import com.gckavach.gckavachapp.incident.api.IncidentTimelineResponse;
import com.gckavach.gckavachapp.incident.domain.IncidentSeverity;
import com.gckavach.gckavachapp.incident.domain.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IncidentService {

    IncidentResponse createIncident(
            IncidentCreateRequest request
    );
    Page<IncidentResponse> queryIncidents(
            IncidentStatus status,
            IncidentSeverity severity,
            String serviceName,
            String environment,
            Pageable pageable
    );


    IncidentResponse acknowledgeIncident(String incidentId);

    IncidentResponse startInvestigation(String incidentId);

    IncidentResponse mitigateIncident(String incidentId);

    IncidentResponse resolveIncident(String incidentId);

    IncidentResponse closeIncident(String incidentId);

    IncidentResponse assignIncident(
            String incidentId,
            String assignedTo
    );

    List<IncidentTimelineResponse> getIncidentTimeline(
            String incidentId
    );

    /**
     * Performs dynamic incident search.
     */
    Page<IncidentResponse> searchIncidents(
            IncidentSearchCriteria criteria,
            Pageable pageable
    );
}