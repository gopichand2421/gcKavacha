package com.gckavach.gckavachapp.alert.event;

import com.gckavach.gckavachapp.alert.domain.AlertSeverity;
import com.gckavach.gckavachapp.alert.domain.AlertStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Document(collection = "alert_event_audits")
public class AlertEventAudit {

    @Id
    private String id;

    @Indexed
    private String eventId;

    @Indexed
    private String alertId;

    private AlertEventType eventType;

    private Instant occurredAt;

    private String externalAlertId;

    private String title;

    private String source;

    private AlertSeverity severity;

    private AlertStatus status;

    private String serviceName;

    private String environment;

    private String fingerprint;

    private Instant createdAt;

    protected AlertEventAudit() {
    }

    public AlertEventAudit(AlertEvent event) {
        this.eventId = event.eventId();
        this.alertId = event.alterId();
        this.eventType = event.eventType();
        this.occurredAt = event.occurredAt();
        this.externalAlertId = event.externalAlertId();
        this.title = event.title();
        this.source = event.source();
        this.severity = event.severity();
        this.status = event.status();
        this.serviceName = event.serviceName();
        this.environment = event.environment();
        this.fingerprint = event.fingerprint();
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getAlertId() {
        return alertId;
    }

    public AlertEventType getEventType() {
        return eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getExternalAlertId() {
        return externalAlertId;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}