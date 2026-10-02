package com.gckavach.gckavachapp.alert.service;

/**
 * Service responsible for alert deduplication.
 *
 * The purpose of this service is to make sure that
 * multiple identical alerts do not create multiple
 * active alerts for the same fingerprint.
 */
public interface AlertDeduplicationService {

    /**
     * Claims a fingerprint for an active alert.
     *
     * If another active alert already owns the fingerprint,
     * the implementation should throw AlertConflictException.
     *
     * @param fingerprint alert fingerprint
     * @param alertId      alert ID
     */
    void acquire(
            String fingerprint,
            String alertId
    );

    /**
     * Releases a fingerprint.
     *
     * After release, another alert with the same fingerprint
     * can become active.
     *
     * @param fingerprint alert fingerprint
     */
    void release(
            String fingerprint
    );

    /**
     * Checks whether a fingerprint currently belongs
     * to an active alert.
     *
     * @param fingerprint alert fingerprint
     * @return true when fingerprint is active
     */
    boolean isActive(
            String fingerprint
    );
}