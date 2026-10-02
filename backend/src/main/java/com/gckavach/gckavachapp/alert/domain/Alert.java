package com.gckavach.gckavachapp.alert.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a production alert in GcKavacha.
 *
 * An Alert represents an operational event that may require
 * investigation and remediation.
 *
 * Alert lifecycle:
 *
 * OPEN
 *   ├── ACKNOWLEDGED
 *   │       ├── RESOLVED
 *   │       └── SUPPRESSED
 *   │
 *   ├── RESOLVED
 *   │
 *   └── SUPPRESSED
 *
 * The fingerprint is used by the alert deduplication mechanism
 * to prevent multiple active alerts representing the same
 * underlying incident.
 */
@Document(collection = "alerts")
public class Alert {

    /**
     * MongoDB document identifier.
     */
    @Id
    private String id;

    /**
     * Identifier supplied by the external alerting system.
     *
     * Examples:
     *
     * - Prometheus alert ID
     * - Datadog alert ID
     * - PagerDuty alert ID
     * - Custom monitoring system ID
     */
    @Indexed
    private String externalAlertId;

    /**
     * Human-readable alert title.
     */
    private String title;

    /**
     * Detailed description of the alert.
     */
    private String description;

    /**
     * Source that generated the alert.
     *
     * Examples:
     *
     * - Prometheus
     * - Grafana
     * - Datadog
     * - Application
     * - Kubernetes
     */
    @Indexed
    private String source;

    /**
     * Severity of the alert.
     */
    @Indexed
    private AlertSeverity severity;

    /**
     * Current lifecycle status of the alert.
     */
    @Indexed
    private AlertStatus status;

    /**
     * Service affected by the alert.
     */
    @Indexed
    private String serviceName;

    /**
     * Environment in which the alert occurred.
     *
     * Examples:
     *
     * - development
     * - staging
     * - production
     */
    @Indexed
    private String environment;

    /**
     * Deterministic fingerprint used for alert deduplication.
     *
     * The fingerprint is generated from:
     *
     * source + serviceName + environment + title
     *
     * The fingerprint itself is stored on the Alert so that
     * the deduplication record can later be released when
     * the alert is resolved or suppressed.
     */
    @Indexed
    private String fingerprint;

    /**
     * Additional alert labels.
     *
     * Examples:
     *
     * service=payment-service
     * region=ap-south-1
     * team=payments
     */
    private Map<String, String> labels;

    /**
     * Additional alert annotations.
     *
     * Examples:
     *
     * runbook=https://...
     * dashboard=https://...
     * owner=payments-team
     */
    private Map<String, String> annotations;

    /**
     * Time when the alert started.
     */
    private Instant startedAt;

    /**
     * Time when the alert was acknowledged.
     */
    private Instant acknowledgedAt;

    /**
     * Time when the alert was resolved.
     */
    private Instant resolvedAt;

    /**
     * Time when the alert was created.
     */
    private Instant createdAt;

    /**
     * Time when the alert was last updated.
     */
    private Instant updatedAt;

    /**
     * Default constructor required by Spring Data MongoDB.
     */
    protected Alert() {
    }

    /**
     * Creates a new Alert.
     *
     * A newly created alert always starts in OPEN state.
     *
     * @param externalAlertId external alert identifier
     * @param title alert title
     * @param description alert description
     * @param source alert source
     * @param severity alert severity
     * @param serviceName affected service
     * @param environment affected environment
     * @param fingerprint initial fingerprint value
     */
    public Alert(
            String externalAlertId,
            String title,
            String description,
            String source,
            AlertSeverity severity,
            String serviceName,
            String environment,
            String fingerprint) {

        this.externalAlertId = externalAlertId;
        this.title = title;
        this.description = description;
        this.source = source;
        this.severity = severity;
        this.status = AlertStatus.OPEN;
        this.serviceName = serviceName;
        this.environment = environment;
        this.fingerprint = fingerprint;

        this.labels = new HashMap<>();
        this.annotations = new HashMap<>();

        this.startedAt = Instant.now();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Acknowledges the alert.
     *
     * Valid transition:
     *
     * OPEN -> ACKNOWLEDGED
     *
     * An already acknowledged, resolved or suppressed alert
     * cannot be acknowledged again.
     */
    public void acknowledge() {

        if (this.status != AlertStatus.OPEN) {
            throw new IllegalStateException(
                    "Alert cannot be acknowledged from status: "
                            + this.status
            );
        }

        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Resolves the alert.
     *
     * Valid transitions:
     *
     * OPEN -> RESOLVED
     *
     * ACKNOWLEDGED -> RESOLVED
     */
    public void resolve() {

        if (this.status != AlertStatus.OPEN
                && this.status != AlertStatus.ACKNOWLEDGED) {

            throw new IllegalStateException(
                    "Alert cannot be resolved from status: "
                            + this.status
            );
        }

        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Suppresses the alert.
     *
     * Valid transitions:
     *
     * OPEN -> SUPPRESSED
     *
     * ACKNOWLEDGED -> SUPPRESSED
     */
    public void suppress() {

        if (this.status != AlertStatus.OPEN
                && this.status != AlertStatus.ACKNOWLEDGED) {

            throw new IllegalStateException(
                    "Alert cannot be suppressed from status: "
                            + this.status
            );
        }

        this.status = AlertStatus.SUPPRESSED;
        this.updatedAt = Instant.now();
    }

    // ============================================================
    // Getters
    // ============================================================

    /**
     * Returns the MongoDB ID.
     *
     * @return alert ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the external alert ID.
     *
     * @return external alert ID
     */
    public String getExternalAlertId() {
        return externalAlertId;
    }

    /**
     * Returns the alert title.
     *
     * @return alert title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the alert description.
     *
     * @return alert description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the alert source.
     *
     * @return alert source
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the alert severity.
     *
     * @return alert severity
     */
    public AlertSeverity getSeverity() {
        return severity;
    }

    /**
     * Returns the current alert status.
     *
     * @return alert status
     */
    public AlertStatus getStatus() {
        return status;
    }

    /**
     * Returns the affected service name.
     *
     * @return service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Returns the environment.
     *
     * @return environment
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Returns the deduplication fingerprint.
     *
     * @return fingerprint
     */
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * Returns alert labels.
     *
     * @return labels
     */
    public Map<String, String> getLabels() {
        return labels;
    }

    /**
     * Returns alert annotations.
     *
     * @return annotations
     */
    public Map<String, String> getAnnotations() {
        return annotations;
    }

    /**
     * Returns the alert start time.
     *
     * @return started timestamp
     */
    public Instant getStartedAt() {
        return startedAt;
    }

    /**
     * Returns the acknowledgement timestamp.
     *
     * @return acknowledged timestamp
     */
    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    /**
     * Returns the resolution timestamp.
     *
     * @return resolved timestamp
     */
    public Instant getResolvedAt() {
        return resolvedAt;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return created timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the last update timestamp.
     *
     * @return updated timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // ============================================================
    // Setters
    // ============================================================

    /**
     * Sets the MongoDB ID.
     *
     * @param id alert ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the external alert ID.
     *
     * @param externalAlertId external alert ID
     */
    public void setExternalAlertId(String externalAlertId) {
        this.externalAlertId = externalAlertId;
    }

    /**
     * Sets the alert title.
     *
     * @param title alert title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the alert description.
     *
     * @param description alert description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the alert source.
     *
     * @param source alert source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Sets the alert severity.
     *
     * @param severity alert severity
     */
    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    /**
     * Sets the alert status.
     *
     * @param status alert status
     */
    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    /**
     * Sets the affected service name.
     *
     * @param serviceName service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Sets the environment.
     *
     * @param environment environment
     */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * Sets the alert fingerprint.
     *
     * This setter is required by DefaultAlertService when the
     * fingerprint is generated during alert creation.
     *
     * @param fingerprint alert fingerprint
     */
    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    /**
     * Sets alert labels.
     *
     * @param labels alert labels
     */
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    /**
     * Sets alert annotations.
     *
     * @param annotations alert annotations
     */
    public void setAnnotations(
            Map<String, String> annotations) {

        this.annotations = annotations;
    }

    /**
     * Sets the alert start time.
     *
     * @param startedAt start timestamp
     */
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * Sets the acknowledgement timestamp.
     *
     * @param acknowledgedAt acknowledgement timestamp
     */
    public void setAcknowledgedAt(
            Instant acknowledgedAt) {

        this.acknowledgedAt = acknowledgedAt;
    }

    /**
     * Sets the resolution timestamp.
     *
     * @param resolvedAt resolution timestamp
     */
    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Sets the last update timestamp.
     *
     * @param updatedAt last update timestamp
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}