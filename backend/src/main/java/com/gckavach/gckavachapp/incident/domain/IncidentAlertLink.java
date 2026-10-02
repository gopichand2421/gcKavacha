package com.gckavach.gckavachapp.incident.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "incident_alert_links")
@CompoundIndex(
        name = "incident_alert_unique_idx",
        def = "{'incidentId': 1, 'alertId': 1}",
        unique = true
)
public class IncidentAlertLink {

    @Id
    private String id;

    private String incidentId;

    private String alertId;

    private Instant createdAt;

    protected IncidentAlertLink() {
        // Required by MongoDB
    }

    public IncidentAlertLink(
            String incidentId,
            String alertId) {

        this.incidentId = incidentId;
        this.alertId = alertId;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public String getAlertId() {
        return alertId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}