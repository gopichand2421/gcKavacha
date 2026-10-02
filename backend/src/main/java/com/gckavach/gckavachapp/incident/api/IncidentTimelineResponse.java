package com.gckavach.gckavachapp.incident.api;

import com.gckavach.gckavachapp.incident.audit.IncidentEventAudit;

import java.time.Instant;

/**
 * API representation of an incident timeline event.
 *
 * <p>This DTO intentionally exposes only the information required
 * by clients to display the incident history.</p>
 */
public record IncidentTimelineResponse(

        /**
         * Unique event identifier.
         */
        String eventId,

        /**
         * Type of incident event.
         */
        String eventType,

        /**
         * Time at which the event occurred.
         */
        Instant occurredAt,

        /**
         * Incident status after the event.
         */
        String status,

        /**
         * User assigned to the incident at the time of the event.
         */
        String assignedTo
) {

    /**
     * Converts a persisted audit record into an API response.
     *
     * @param audit persisted incident event audit
     * @return timeline response
     */
    public static IncidentTimelineResponse from(
            IncidentEventAudit audit) {

        return new IncidentTimelineResponse(
                audit.getEventId(),
                audit.getEventType(),
                audit.getOccurredAt(),
                audit.getStatus(),
                audit.getAssignedTo()
        );
    }
}