package com.gckavach.gckavachapp.incident.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "incidents")
public class Incident {

    @Id
    private String id;

    @Indexed(unique = true)
    private String incidentNumber;

    private String title;

    private String description;

    private IncidentSeverity severity;

    private IncidentStatus status;

    private String serviceName;

    private String environment;

    private String assignedTo;

    private Instant detectedAt;

    private Instant investigatingAt;

    private Instant mitigatedAt;

    private Instant resolvedAt;

    private Instant closedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Default constructor required by Spring Data MongoDB.
     */
    public  Incident() {
        Instant now = Instant.now();

        this.status = IncidentStatus.OPEN;
        this.detectedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }


    /**
     * Creates a new incident.
     *
     * New incidents always start in OPEN state.
     */
    public Incident(
            String incidentNumber,
            String title,
            String description,
            IncidentSeverity severity,
            String serviceName,
            String environment
    ) {
        this.incidentNumber = incidentNumber;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = IncidentStatus.OPEN;
        this.serviceName = serviceName;
        this.environment = environment;

        Instant now = Instant.now();

        this.detectedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Acknowledge the incident.
     *
     * Valid transition:
     *
     * OPEN -> ACKNOWLEDGED
     */
    public void acknowledge() {

        if (this.status != IncidentStatus.OPEN) {
            throw new IllegalStateException(
                    "Incident can only be acknowledged when it is OPEN"
            );
        }

        this.status = IncidentStatus.ACKNOWLEDGED;
        this.updatedAt = Instant.now();
    }

    /**
     * Start investigation.
     *
     * Valid transition:
     *
     * ACKNOWLEDGED -> INVESTIGATING
     */
    public void startInvestigation() {

        if (this.status != IncidentStatus.ACKNOWLEDGED) {
            throw new IllegalStateException(
                    "Incident can only start investigation when it is ACKNOWLEDGED"
            );
        }

        this.status = IncidentStatus.INVESTIGATING;
        this.investigatingAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Mark the incident as mitigated.
     *
     * Valid transition:
     *
     * INVESTIGATING -> MITIGATED
     */
    public void mitigate() {

        if (this.status != IncidentStatus.INVESTIGATING) {
            throw new IllegalStateException(
                    "Incident can only be mitigated when it is INVESTIGATING"
            );
        }

        this.status = IncidentStatus.MITIGATED;
        this.mitigatedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Resolve the incident.
     *
     * Valid transition:
     *
     * MITIGATED -> RESOLVED
     */
    public void resolve() {

        if (this.status != IncidentStatus.MITIGATED) {
            throw new IllegalStateException(
                    "Incident can only be resolved when it is MITIGATED"
            );
        }

        this.status = IncidentStatus.RESOLVED;
        this.resolvedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Close the incident.
     *
     * Valid transition:
     *
     * RESOLVED -> CLOSED
     */
    public void close() {

        if (this.status != IncidentStatus.RESOLVED) {
            throw new IllegalStateException(
                    "Incident can only be closed when it is RESOLVED"
            );
        }

        this.status = IncidentStatus.CLOSED;
        this.closedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Assign the incident to a user.
     */
    public void assignTo(String assignedTo) {

        if (assignedTo == null || assignedTo.isBlank()) {
            throw new IllegalArgumentException(
                    "Assigned user must not be blank"
            );
        }

        this.assignedTo = assignedTo.trim();
        this.updatedAt = Instant.now();
    }

    // ---------------------------------------------------------
    // Getters
    // ---------------------------------------------------------

    public String getId() {
        return id;
    }

    public String getIncidentNumber() {
        return incidentNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public Instant getInvestigatingAt() {
        return investigatingAt;
    }

    public Instant getMitigatedAt() {
        return mitigatedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // ---------------------------------------------------------
    // Setters
    // ---------------------------------------------------------

    public void setId(String id) {
        this.id = id;
    }

    public void setIncidentNumber(String incidentNumber) {
        this.incidentNumber = incidentNumber;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
        this.updatedAt = Instant.now();
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
        this.updatedAt = Instant.now();
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
        this.updatedAt = Instant.now();
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
        this.updatedAt = Instant.now();
    }

    public void setDetectedAt(Instant detectedAt) {
        this.detectedAt = detectedAt;
        this.updatedAt = Instant.now();
    }

    public void setInvestigatingAt(Instant investigatingAt) {
        this.investigatingAt = investigatingAt;
        this.updatedAt = Instant.now();
    }

    public void setMitigatedAt(Instant mitigatedAt) {
        this.mitigatedAt = mitigatedAt;
        this.updatedAt = Instant.now();
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
        this.updatedAt = Instant.now();
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
        this.updatedAt = Instant.now();
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}