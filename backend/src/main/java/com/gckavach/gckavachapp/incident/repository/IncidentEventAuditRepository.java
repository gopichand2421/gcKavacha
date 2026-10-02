package com.gckavach.gckavachapp.incident.repository;

import com.gckavach.gckavachapp.incident.audit.IncidentEventAudit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for incident event audit records.
 */
public interface IncidentEventAuditRepository
        extends MongoRepository<IncidentEventAudit, String> {

    /**
     * Finds an audit record using the unique event ID.
     */
    Optional<IncidentEventAudit> findByEventId(String eventId);

    /**
     * Checks whether an event has already been processed.
     *
     * <p>This prevents duplicate audit records when Kafka
     * redelivers an event.</p>
     */
    boolean existsByEventId(String eventId);

    /**
     * Returns all events for an incident in chronological order.
     *
     * @param incidentId incident ID
     * @return ordered incident event history
     */
    List<IncidentEventAudit> findByIncidentIdOrderByOccurredAtAsc(
            String incidentId
    );
}