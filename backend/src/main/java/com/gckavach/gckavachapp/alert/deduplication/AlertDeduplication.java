package com.gckavach.gckavachapp.alert.deduplication;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents an active deduplication record for an alert.
 *
 * A fingerprint can belong to only one active alert at a time.
 *
 * Example:
 *
 * fingerprint = "abc123"
 * alertId     = "alert-001"
 *
 * If another alert arrives with fingerprint "abc123"
 * while this record exists, it will be treated as a duplicate.
 */
@Document(collection = "alert_deduplications")
public class AlertDeduplication {

    /**
     * MongoDB document ID.
     */
    @Id
    private String id;

    /**
     * Unique fingerprint of the active alert.
     *
     * The unique index is important because it protects us
     * against concurrent duplicate alert creation.
     */
    @Indexed(unique = true)
    private String fingerprint;

    /**
     * ID of the alert currently owning this fingerprint.
     */
    private String alertId;

    /**
     * Time when the deduplication record was created.
     */
    private Instant createdAt;

    /**
     * Required by Spring Data MongoDB.
     */
    protected AlertDeduplication() {
    }

    /**
     * Creates a new deduplication record.
     *
     * @param fingerprint alert fingerprint
     * @param alertId      alert ID
     */
    public AlertDeduplication(
            String fingerprint,
            String alertId) {

        this.fingerprint = fingerprint;
        this.alertId = alertId;
        this.createdAt = Instant.now();
    }

    // ---------------------------------------------------------
    // Getters
    // ---------------------------------------------------------

    public String getId() {
        return id;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getAlertId() {
        return alertId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // ---------------------------------------------------------
    // Setters
    // ---------------------------------------------------------

    public void setId(String id) {
        this.id = id;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}