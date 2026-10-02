package com.gckavach.gckavachapp.alert.deduplication;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository for managing active alert deduplication records.
 */
public interface AlertDeduplicationRepository
        extends MongoRepository<AlertDeduplication, String> {

    /**
     * Finds the active deduplication record for a fingerprint.
     *
     * @param fingerprint alert fingerprint
     * @return matching deduplication record
     */
    Optional<AlertDeduplication> findByFingerprint(
            String fingerprint
    );

    /**
     * Checks whether an active alert currently owns
     * the supplied fingerprint.
     *
     * @param fingerprint alert fingerprint
     * @return true when fingerprint is active
     */
    boolean existsByFingerprint(
            String fingerprint
    );

    /**
     * Releases the fingerprint.
     *
     * This is normally called when an alert is resolved
     * or suppressed.
     *
     * @param fingerprint alert fingerprint
     */
    void deleteByFingerprint(
            String fingerprint
    );
}