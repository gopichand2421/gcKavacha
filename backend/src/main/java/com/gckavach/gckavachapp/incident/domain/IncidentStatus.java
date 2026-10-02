package com.gckavach.gckavachapp.incident.domain;

/**
 * Lifecycle states of an incident.
 */
public enum IncidentStatus {

    OPEN,

    ACKNOWLEDGED,

    INVESTIGATING,

    MITIGATED,

    RESOLVED,

    CLOSED
}