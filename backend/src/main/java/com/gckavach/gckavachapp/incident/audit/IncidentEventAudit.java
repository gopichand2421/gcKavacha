package com.gckavach.gckavachapp.incident.audit;

import com.gckavach.gckavachapp.incident.event.IncidentEvent;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents an immutable audit record for an incident event.
 *
 * <p>Every consumed incident event is stored so that GcKavacha
 * can reconstruct the history of an incident.</p>
 *
 * <p>The eventId is unique and is used as the idempotency key.
 * If Kafka delivers the same event more than once, only one
 * audit record should be persisted.</p>
 */
@Document(collection = "incident_event_audits")
public class IncidentEventAudit {

    @Id
    private String id;

    /**
     * Unique identifier of the event.
     */
    @Indexed(unique = true)
    private String eventId;

    /**
     * ID of the incident associated with this event.
     */
    @Indexed
    private String incidentId;

    /**
     * Business identifier of the incident.
     *
     * <p>Example: INC-2026-001</p>
     */
    private String incidentNumber;

    /**
     * Type of incident event.
     */
    private String eventType;

    /**
     * Time when the event occurred.
     */
    private Instant occurredAt;

    /**
     * Time when the consumer persisted the audit record.
     */
    private Instant consumedAt;

    /**
     * Incident status when the event occurred.
     */
    private String status;

    /**
     * User assigned to the incident at the time of the event.
     */
    private String assignedTo;

    /**
     * Default constructor required by MongoDB.
     */
    public IncidentEventAudit() {
    }

    /**
     * Creates an audit record from an incident event.
     *
     * @param event incident event received from Kafka
     * @return audit entity
     */
    public static IncidentEventAudit from(IncidentEvent event) {

        IncidentEventAudit audit = new IncidentEventAudit();

        audit.setEventId(event.eventId());
        audit.setIncidentId(event.incidentId());
        audit.setIncidentNumber(event.incidentNumber());
        audit.setEventType(event.eventType().name());
        audit.setOccurredAt(event.occurredAt());
        audit.setConsumedAt(Instant.now());

        if (event.status() != null) {
            audit.setStatus(event.status().name());
        }

        audit.setAssignedTo(event.assignedTo());

        return audit;
    }

    // ---------------------------------------------------------
    // Getters
    // ---------------------------------------------------------

    public String getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public String getIncidentNumber() {
        return incidentNumber;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public String getStatus() {
        return status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    // ---------------------------------------------------------
    // Setters
    // ---------------------------------------------------------

    public void setId(String id) {
        this.id = id;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public void setIncidentNumber(String incidentNumber) {
        this.incidentNumber = incidentNumber;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public void setConsumedAt(Instant consumedAt) {
        this.consumedAt = consumedAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}