package com.gckavach.gckavachapp.alert.correlation;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents a persisted relationship between two related alerts.
 *
 * An AlertCorrelation does not change the Alert itself.
 * It simply records that two alerts are related.
 */
@Document(collection = "alert_correlations")
@CompoundIndex(
        name = "correlation_pair_unique",
        def = "{'primaryAlertId': 1, 'relatedAlertId': 1}",
        unique = true
)
public class AlertCorrelation {

    @Id
    private String id;

    @Indexed
    private String primaryAlertId;

    @Indexed
    private String relatedAlertId;

    private String correlationType;

    private long timeDifferenceSeconds;

    @CreatedDate
    private Instant createdAt;

    protected AlertCorrelation() {
    }

    public AlertCorrelation(
            String primaryAlertId,
            String relatedAlertId,
            String correlationType,
            long timeDifferenceSeconds) {

        this.primaryAlertId = primaryAlertId;
        this.relatedAlertId = relatedAlertId;
        this.correlationType = correlationType;
        this.timeDifferenceSeconds = timeDifferenceSeconds;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getPrimaryAlertId() {
        return primaryAlertId;
    }

    public String getRelatedAlertId() {
        return relatedAlertId;
    }

    public String getCorrelationType() {
        return correlationType;
    }

    public long getTimeDifferenceSeconds() {
        return timeDifferenceSeconds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPrimaryAlertId(String primaryAlertId) {
        this.primaryAlertId = primaryAlertId;
    }

    public void setRelatedAlertId(String relatedAlertId) {
        this.relatedAlertId = relatedAlertId;
    }

    public void setCorrelationType(String correlationType) {
        this.correlationType = correlationType;
    }

    public void setTimeDifferenceSeconds(long timeDifferenceSeconds) {
        this.timeDifferenceSeconds = timeDifferenceSeconds;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}