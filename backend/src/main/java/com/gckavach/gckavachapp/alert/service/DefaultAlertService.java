package com.gckavach.gckavachapp.alert.service;

import com.gckavach.gckavachapp.alert.api.AlertCreateRequest;
import com.gckavach.gckavachapp.alert.api.AlertResponse;
import com.gckavach.gckavachapp.alert.domain.Alert;
import com.gckavach.gckavachapp.alert.domain.AlertSeverity;
import com.gckavach.gckavachapp.alert.domain.AlertStatus;
import com.gckavach.gckavachapp.alert.event.AlertEventPublisher;
import com.gckavach.gckavachapp.alert.event.AlertEventType;
import com.gckavach.gckavachapp.alert.repository.AlertRepository;
import com.gckavach.gckavachapp.common.exception.AlertNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Default implementation of AlertService.
 *
 * Responsibilities:
 *
 * 1. Create alerts.
 * 2. Generate alert fingerprints.
 * 3. Prevent duplicate active alerts.
 * 4. Persist alerts.
 * 5. Publish alert lifecycle events.
 * 6. Query alerts.
 * 7. Manage alert lifecycle.
 * 8. Release deduplication records when alerts are resolved
 *    or suppressed.
 */
@Service
public class DefaultAlertService implements AlertService {

    /**
     * Repository used to persist and query alerts.
     */
    private final AlertRepository alertRepository;

    /**
     * Publisher used to publish alert events to Kafka.
     */
    private final AlertEventPublisher alertEventPublisher;

    /**
     * Service used to manage alert deduplication.
     */
    private final AlertDeduplicationService alertDeduplicationService;

    /**
     * Constructor.
     *
     * @param alertRepository alert repository
     * @param alertEventPublisher alert event publisher
     * @param alertDeduplicationService alert deduplication service
     */
    public DefaultAlertService(
            AlertRepository alertRepository,
            AlertEventPublisher alertEventPublisher,
            AlertDeduplicationService alertDeduplicationService) {

        this.alertRepository = alertRepository;
        this.alertEventPublisher = alertEventPublisher;
        this.alertDeduplicationService = alertDeduplicationService;
    }

    /**
     * Creates a new alert.
     *
     * The fingerprint is acquired before the alert is persisted.
     * MongoDB's unique fingerprint index prevents two concurrent
     * requests from creating the same active alert.
     *
     * @param request alert creation request
     * @return created alert response
     */
    @Override
    public AlertResponse createAlert(
            AlertCreateRequest request) {

        // Validate the incoming request.
        validateCreateRequest(request);

        /*
         * Generate a deterministic fingerprint.
         *
         * The same source, service, environment and title
         * will always produce the same fingerprint.
         */
        String fingerprint = generateFingerprint(
                request.source(),
                request.serviceName(),
                request.environment(),
                request.title()
        );

        /*
         * MongoDB generates the actual Alert ID when the Alert
         * is persisted.
         *
         * Therefore, use a temporary ID while acquiring the
         * deduplication record.
         */
        String temporaryAlertId = UUID
                .randomUUID()
                .toString();

        /*
         * Acquire the fingerprint before creating the Alert.
         *
         * If another active Alert already owns this fingerprint,
         * AlertDeduplicationService will throw AlertConflictException.
         */
        alertDeduplicationService.acquire(
                fingerprint,
                temporaryAlertId
        );

        try {

            /*
             * Create the Alert domain object.
             *
             * The final fingerprint argument is provided
             * by the generated fingerprint.
             */
            Alert alert = new Alert(
                    request.externalAlertId(),
                    request.title(),
                    request.description(),
                    request.source(),
                    request.severity(),
                    request.serviceName(),
                    request.environment(),
                    fingerprint
            );

            /*
             * Persist the Alert.
             */
            Alert savedAlert =
                    alertRepository.save(alert);

            /*
             * Publish ALERT_CREATED only after successful
             * persistence.
             */
            alertEventPublisher.publish(
                    AlertEventType.ALERT_CREATED,
                    savedAlert
            );

            /*
             * Return the persisted Alert.
             */
            return AlertResponse.from(savedAlert);

        } catch (RuntimeException exception) {

            /*
             * Persistence or Kafka publishing failed.
             *
             * Release the fingerprint so the request can
             * safely be retried.
             */
            alertDeduplicationService.release(
                    fingerprint
            );

            /*
             * Preserve the original exception.
             */
            throw exception;
        }
    }

    /**
     * Queries alerts using the supported filters.
     *
     * Supported filters:
     *
     * - status
     * - severity
     * - serviceName
     * - environment
     *
     * @param status alert status
     * @param severity alert severity
     * @param serviceName service name
     * @param environment environment
     * @param pageable pagination information
     * @return paginated alert responses
     */
    @Override
    public Page<AlertResponse> queryAlerts(
            AlertStatus status,
            AlertSeverity severity,
            String serviceName,
            String environment,
            Pageable pageable) {

        Page<Alert> alerts;

        /*
         * Status + Severity + Service + Environment
         */
        if (status != null
                && severity != null
                && serviceName != null
                && environment != null) {

            alerts =
                    alertRepository
                            .findByStatusAndSeverityAndServiceNameAndEnvironment(
                                    status,
                                    severity,
                                    serviceName,
                                    environment,
                                    pageable
                            );

            /*
             * Status + Severity + Service
             */
        } else if (status != null
                && severity != null
                && serviceName != null) {

            alerts =
                    alertRepository
                            .findByStatusAndSeverityAndServiceName(
                                    status,
                                    severity,
                                    serviceName,
                                    pageable
                            );

            /*
             * Status + Severity
             */
        } else if (status != null
                && severity != null) {

            alerts =
                    alertRepository
                            .findByStatusAndSeverity(
                                    status,
                                    severity,
                                    pageable
                            );

            /*
             * Status + Service
             */
        } else if (status != null
                && serviceName != null) {

            alerts =
                    alertRepository
                            .findByStatusAndServiceName(
                                    status,
                                    serviceName,
                                    pageable
                            );

            /*
             * Status + Environment
             */
        } else if (status != null
                && environment != null) {

            alerts =
                    alertRepository
                            .findByStatusAndEnvironment(
                                    status,
                                    environment,
                                    pageable
                            );

            /*
             * Severity + Service
             */
        } else if (severity != null
                && serviceName != null) {

            alerts =
                    alertRepository
                            .findBySeverityAndServiceName(
                                    severity,
                                    serviceName,
                                    pageable
                            );

            /*
             * Service + Environment
             */
        } else if (serviceName != null
                && environment != null) {

            alerts =
                    alertRepository
                            .findByServiceNameAndEnvironment(
                                    serviceName,
                                    environment,
                                    pageable
                            );

            /*
             * Status only
             */
        } else if (status != null) {

            alerts =
                    alertRepository.findByStatus(
                            status,
                            pageable
                    );

            /*
             * Severity only
             */
        } else if (severity != null) {

            alerts =
                    alertRepository.findBySeverity(
                            severity,
                            pageable
                    );

            /*
             * Service only
             */
        } else if (serviceName != null) {

            alerts =
                    alertRepository.findByServiceName(
                            serviceName,
                            pageable
                    );

            /*
             * Environment only
             */
        } else if (environment != null) {

            alerts =
                    alertRepository.findByEnvironment(
                            environment,
                            pageable
                    );

            /*
             * No filters.
             */
        } else {

            alerts =
                    alertRepository.findAll(pageable);
        }

        /*
         * Convert Alert entities into API responses.
         */
        return alerts.map(AlertResponse::from);
    }

    /**
     * Finds an alert by ID.
     *
     * @param alertId alert ID
     * @return Alert response
     * @throws AlertNotFoundException if the alert does not exist
     */
    @Override
    public AlertResponse getAlert(
            String alertId) {

        if (alertId == null || alertId.isBlank()) {
            throw new IllegalArgumentException(
                    "Alert ID is required"
            );
        }

        Alert alert =
                alertRepository
                        .findById(alertId)
                        .orElseThrow(() ->
                                new AlertNotFoundException(
                                        "Alert not found: " + alertId
                                )
                        );

        return AlertResponse.from(alert);
    }

    /**
     * Acknowledges an alert.
     *
     * An acknowledged alert remains active.
     *
     * Therefore, the fingerprint is NOT released.
     *
     * @param alertId alert ID
     * @return acknowledged alert response
     */
    @Override
    public AlertResponse acknowledgeAlert(
            String alertId) {

        Alert alert =
                getAlertEntity(alertId);

        /*
         * OPEN -> ACKNOWLEDGED
         *
         * The Alert domain object validates the transition.
         */
        alert.acknowledge();

        /*
         * Persist the updated alert.
         */
        Alert savedAlert =
                alertRepository.save(alert);

        /*
         * Publish lifecycle event.
         */
        alertEventPublisher.publish(
                AlertEventType.ALERT_ACKNOWLEDGED,
                savedAlert
        );

        /*
         * Do not release deduplication here.
         *
         * ACKNOWLEDGED is still an active alert state.
         */
        return AlertResponse.from(savedAlert);
    }

    /**
     * Resolves an alert.
     *
     * A resolved alert is no longer active.
     *
     * Therefore, its fingerprint is released.
     *
     * @param alertId alert ID
     * @return resolved alert response
     */
    @Override
    public AlertResponse resolveAlert(
            String alertId) {

        Alert alert =
                getAlertEntity(alertId);

        /*
         * OPEN -> RESOLVED
         *
         * or
         *
         * ACKNOWLEDGED -> RESOLVED
         */
        alert.resolve();

        /*
         * Persist the updated alert.
         */
        Alert savedAlert =
                alertRepository.save(alert);

        /*
         * The alert is no longer active.
         *
         * Release the fingerprint.
         */
        alertDeduplicationService.release(
                savedAlert.getFingerprint()
        );

        /*
         * Publish lifecycle event.
         */
        alertEventPublisher.publish(
                AlertEventType.ALERT_RESOLVED,
                savedAlert
        );

        /*
         * Return the updated Alert.
         */
        return AlertResponse.from(savedAlert);
    }

    /**
     * Suppresses an alert.
     *
     * A suppressed alert is no longer active.
     *
     * Therefore, its fingerprint is released.
     *
     * @param alertId alert ID
     * @return suppressed alert response
     */
    @Override
    public AlertResponse suppressAlert(
            String alertId) {

        Alert alert =
                getAlertEntity(alertId);

        /*
         * OPEN -> SUPPRESSED
         *
         * or
         *
         * ACKNOWLEDGED -> SUPPRESSED
         */
        alert.suppress();

        /*
         * Persist the updated alert.
         */
        Alert savedAlert =
                alertRepository.save(alert);

        /*
         * Suppressed alerts are no longer active.
         *
         * Release the fingerprint.
         */
        alertDeduplicationService.release(
                savedAlert.getFingerprint()
        );

        /*
         * Publish lifecycle event.
         */
        alertEventPublisher.publish(
                AlertEventType.ALERT_SUPPRESSED,
                savedAlert
        );

        /*
         * Return the updated Alert.
         */
        return AlertResponse.from(savedAlert);
    }

    /**
     * Finds an alert entity by ID.
     *
     * This private method is used by lifecycle operations
     * because the domain object is required for state transitions.
     *
     * @param alertId alert ID
     * @return Alert entity
     */
    private Alert getAlertEntity(
            String alertId) {

        if (alertId == null || alertId.isBlank()) {
            throw new IllegalArgumentException(
                    "Alert ID is required"
            );
        }

        return alertRepository
                .findById(alertId)
                .orElseThrow(() ->
                        new AlertNotFoundException(
                                "Alert not found: " + alertId
                        )
                );
    }

    /**
     * Validates an alert creation request.
     *
     * @param request alert creation request
     */
    private void validateCreateRequest(
            AlertCreateRequest request) {

        /*
         * Request must not be null.
         */
        if (request == null) {
            throw new IllegalArgumentException(
                    "Alert request is required"
            );
        }

        /*
         * Title is mandatory.
         */
        if (isBlank(request.title())) {
            throw new IllegalArgumentException(
                    "Alert title is required"
            );
        }

        /*
         * Source is mandatory.
         */
        if (isBlank(request.source())) {
            throw new IllegalArgumentException(
                    "Alert source is required"
            );
        }

        /*
         * Severity is mandatory.
         */
        if (request.severity() == null) {
            throw new IllegalArgumentException(
                    "Alert severity is required"
            );
        }

        /*
         * Service name is mandatory.
         */
        if (isBlank(request.serviceName())) {
            throw new IllegalArgumentException(
                    "Service name is required"
            );
        }

        /*
         * Environment is mandatory.
         */
        if (isBlank(request.environment())) {
            throw new IllegalArgumentException(
                    "Environment is required"
            );
        }
    }

    /**
     * Checks whether a String is null or blank.
     *
     * @param value String value
     * @return true if null or blank
     */
    private boolean isBlank(
            String value) {

        return value == null || value.isBlank();
    }

    /**
     * Generates a deterministic SHA-256 fingerprint.
     *
     * Fingerprint input:
     *
     * source|serviceName|environment|title
     *
     * Example:
     *
     * prometheus|payment-service|production|Payment API failure
     *
     * @param source alert source
     * @param serviceName affected service
     * @param environment environment
     * @param title alert title
     * @return SHA-256 fingerprint
     */
    private String generateFingerprint(
            String source,
            String serviceName,
            String environment,
            String title) {

        /*
         * Build deterministic fingerprint input.
         */
        String value =
                source + "|"
                        + serviceName + "|"
                        + environment + "|"
                        + title;

        try {

            /*
             * Create SHA-256 digest.
             */
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            /*
             * Convert the input into bytes and calculate
             * the SHA-256 hash.
             */
            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            /*
             * Convert the byte array into a hexadecimal String.
             */
            StringBuilder result =
                    new StringBuilder();

            for (byte currentByte : hash) {

                result.append(
                        String.format(
                                "%02x",
                                currentByte
                        )
                );
            }

            /*
             * Return the final fingerprint.
             */
            return result.toString();

        } catch (NoSuchAlgorithmException exception) {

            /*
             * SHA-256 is guaranteed to be available in
             * standard Java implementations.
             */
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }

    /*
     * ============================================================
     * Getters
     * ============================================================
     */

    public AlertRepository getAlertRepository() {
        return alertRepository;
    }

    public AlertEventPublisher getAlertEventPublisher() {
        return alertEventPublisher;
    }

    public AlertDeduplicationService
    getAlertDeduplicationService() {
        return alertDeduplicationService;
    }
}