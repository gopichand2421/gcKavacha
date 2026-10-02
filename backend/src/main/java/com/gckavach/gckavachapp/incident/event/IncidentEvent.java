package com.gckavach.gckavachapp.incident.event;

import com.gckavach.gckavachapp.incident.domain.IncidentSeverity;
import com.gckavach.gckavachapp.incident.domain.IncidentStatus;

import java.time.Instant;

/**
 * Immutable event representing a change in an incident.
 *
 * <p>This object is the event contract shared between the incident service
 * and downstream event consumers.</p>
 *
 * <p>The event intentionally contains the important incident information so
 * consumers do not always need to query MongoDB after receiving an event.</p>
 */
public record IncidentEvent(

        /**
         * Globally unique identifier for this event.
         */
        String eventId,

        /**
         * Type of incident event.
         */
        IncidentEventType eventType,

        /**
         * Timestamp when the event was created.
         */
        Instant occurredAt,

        /**
         * MongoDB identifier of the incident.
         */
        String incidentId,

        /**
         * Business identifier of the incident.
         *
         * <p>Example: INC-2026-001</p>
         */
        String incidentNumber,

        /**
         * Incident title.
         */
        String title,

        /**
         * Current incident severity.
         */
        IncidentSeverity severity,

        /**
         * Incident status after the operation.
         */
        IncidentStatus status,

        /**
         * Service affected by the incident.
         */
        String serviceName,

        /**
         * Environment where the incident occurred.
         *
         * <p>Example: production, staging, development.</p>
         */
        String environment,

        /**
         * User currently assigned to the incident.
         */
        String assignedTo
) {
}