package com.gckavach.gckavachapp.incident.service;

import com.gckavach.gckavachapp.incident.event.IncidentEvent;

/**
 * Service responsible for persisting incident event audit records.
 */
public interface IncidentEventAuditService {

    /**
     * Records an incident event.
     *
     * <p>The implementation must be idempotent. Processing
     * the same event multiple times must not create duplicates.</p>
     *
     * @param event incident event
     */
    void record(IncidentEvent event);
}