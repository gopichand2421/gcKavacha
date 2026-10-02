package com.gckavach.gckavachapp.alert.service;

import com.gckavach.gckavachapp.alert.deduplication.AlertDeduplication;
import com.gckavach.gckavachapp.alert.deduplication.AlertDeduplicationRepository;
import com.gckavach.gckavachapp.common.exception.AlertConflictException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * Default implementation of alert deduplication.
 *
 * MongoDB's unique index on the fingerprint provides
 * concurrency protection.
 *
 * Example:
 *
 * Request A:
 * fingerprint = ABC
 *        |
 *        +----> MongoDB insert SUCCESS
 *
 * Request B:
 * fingerprint = ABC
 *        |
 *        +----> MongoDB unique index violation
 *                    |
 *                    +----> AlertConflictException
 */
@Service
public class DefaultAlertDeduplicationService
        implements AlertDeduplicationService {

    private final AlertDeduplicationRepository repository;

    /**
     * Constructor injection.
     *
     * @param repository deduplication repository
     */
    public DefaultAlertDeduplicationService(
            AlertDeduplicationRepository repository) {

        this.repository = repository;
    }

    /**
     * Claims a fingerprint for an active alert.
     *
     * We intentionally rely on MongoDB's unique index
     * instead of doing:
     *
     *     exists()
     *     save()
     *
     * because that approach has a race condition.
     *
     * @param fingerprint alert fingerprint
     * @param alertId      alert ID
     */
    @Override
    public void acquire(
            String fingerprint,
            String alertId) {

        validateFingerprint(fingerprint);
        validateAlertId(alertId);

        try {

            AlertDeduplication deduplication =
                    new AlertDeduplication(
                            fingerprint,
                            alertId
                    );

            repository.save(deduplication);

        } catch (DuplicateKeyException exception) {

            throw new AlertConflictException(
                    "An active alert already exists for fingerprint: "
                            + fingerprint
            );
        }
    }

    /**
     * Releases the fingerprint.
     *
     * This should normally happen when the alert reaches
     * a terminal state such as RESOLVED or SUPPRESSED.
     *
     * @param fingerprint alert fingerprint
     */
    @Override
    public void release(
            String fingerprint) {

        if (fingerprint == null || fingerprint.isBlank()) {
            return;
        }

        repository.deleteByFingerprint(fingerprint);
    }

    /**
     * Checks whether the fingerprint is currently active.
     *
     * @param fingerprint alert fingerprint
     * @return true when another active alert owns it
     */
    @Override
    public boolean isActive(
            String fingerprint) {

        if (fingerprint == null || fingerprint.isBlank()) {
            return false;
        }

        return repository.existsByFingerprint(fingerprint);
    }

    /**
     * Validates fingerprint.
     *
     * @param fingerprint alert fingerprint
     */
    private void validateFingerprint(
            String fingerprint) {

        if (fingerprint == null || fingerprint.isBlank()) {

            throw new IllegalArgumentException(
                    "Fingerprint is required"
            );
        }
    }

    /**
     * Validates alert ID.
     *
     * @param alertId alert ID
     */
    private void validateAlertId(
            String alertId) {

        if (alertId == null || alertId.isBlank()) {

            throw new IllegalArgumentException(
                    "Alert ID is required"
            );
        }
    }

    // ---------------------------------------------------------
    // Getter
    // ---------------------------------------------------------

    public AlertDeduplicationRepository getRepository() {
        return repository;
    }
}