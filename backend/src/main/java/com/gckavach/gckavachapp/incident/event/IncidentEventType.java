package com.gckavach.gckavachapp.incident.event;

/**
 * Defines all events that can occur during the lifecycle of an incident.
 *
 * <p>These event types are published to Kafka and can later be consumed by
 * services such as notification, audit, AI/RCA, metrics, and timeline services.</p>
 */
public enum IncidentEventType {

    /**
     * Published when a new incident is created.
     */
    INCIDENT_CREATED,

    /**
     * Published when an incident is assigned to a user.
     */
    INCIDENT_ASSIGNED,

    /**
     * Published when an incident is acknowledged.
     */
    INCIDENT_ACKNOWLEDGED,

    /**
     * Published when investigation of an incident starts.
     */
    INCIDENT_INVESTIGATION_STARTED,

    /**
     * Published when an incident is mitigated.
     */
    INCIDENT_MITIGATED,

    /**
     * Published when an incident is resolved.
     */
    INCIDENT_RESOLVED,

    /**
     * Published when an incident is finally closed.
     */
    INCIDENT_CLOSED
}