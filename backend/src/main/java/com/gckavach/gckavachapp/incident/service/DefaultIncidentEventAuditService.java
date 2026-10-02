package com.gckavach.gckavachapp.incident.service;

import com.gckavach.gckavachapp.incident.audit.IncidentEventAudit;
import com.gckavach.gckavachapp.incident.event.IncidentEvent;
import com.gckavach.gckavachapp.incident.repository.IncidentEventAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Default implementation of incident event audit persistence.
 */
@Service
public class DefaultIncidentEventAuditService
        implements IncidentEventAuditService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    DefaultIncidentEventAuditService.class
            );

    private final IncidentEventAuditRepository auditRepository;

    /**
     * Creates the audit service.
     *
     * @param auditRepository MongoDB repository for audit records
     */
    public DefaultIncidentEventAuditService(
            IncidentEventAuditRepository auditRepository) {

        this.auditRepository = auditRepository;
    }

    /**
     * Persists an incident event as an audit record.
     *
     * <p>Kafka provides at-least-once delivery in typical configurations,
     * so duplicate events are possible. eventId is therefore checked
     * before persistence.</p>
     *
     * @param event incident event
     */
    @Override
    public void record(IncidentEvent event) {

        if (event == null) {
            log.warn("Ignoring null incident event");

            return;
        }

        if (auditRepository.existsByEventId(event.eventId())) {

            log.debug(
                    "Incident event already processed: eventId={}",
                    event.eventId()
            );

            return;
        }

        IncidentEventAudit audit =
                IncidentEventAudit.from(event);

        auditRepository.save(audit);

        log.info(
                "Incident event audit persisted: eventId={}, type={}, incidentId={}",
                event.eventId(),
                event.eventType(),
                event.incidentId()
        );
    }
}