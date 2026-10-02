package com.gckavach.gckavachapp.incident.api;

import com.gckavach.gckavachapp.incident.domain.IncidentAlertLink;

import java.time.Instant;

public record IncidentAlertLinkResponse(
        String linkId,
        String incidentId,
        String alertId,
        Instant createdAt
) {

    public static IncidentAlertLinkResponse from(
            IncidentAlertLink link) {

        return new IncidentAlertLinkResponse(
                link.getId(),
                link.getIncidentId(),
                link.getAlertId(),
                link.getCreatedAt()
        );
    }
}