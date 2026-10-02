package com.gckavach.gckavachapp.incident.api;

import com.gckavach.gckavachapp.incident.domain.Incident;
import com.gckavach.gckavachapp.incident.domain.IncidentSeverity;
import com.gckavach.gckavachapp.incident.domain.IncidentStatus;

import java.time.Instant;

public record IncidentResponse(
        String id,
        String incidentNumber,
        String title,
        String description,
        IncidentSeverity severity,
        IncidentStatus status,
        String serviceName,
        String environment,
        String assignedTo,
        Instant detectedAt,
        Instant investigatingAt,
        Instant mitigatedAt,
        Instant resolvedAt,
        Instant closedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static IncidentResponse from(Incident incident) {

        return new IncidentResponse(
                incident.getId(),
                incident.getIncidentNumber(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getServiceName(),
                incident.getEnvironment(),
                incident.getAssignedTo(),
                incident.getDetectedAt(),
                incident.getInvestigatingAt(),
                incident.getMitigatedAt(),
                incident.getResolvedAt(),
                incident.getClosedAt(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()
        );
    }
}