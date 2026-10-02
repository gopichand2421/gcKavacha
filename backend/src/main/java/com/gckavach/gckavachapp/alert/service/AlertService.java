package com.gckavach.gckavachapp.alert.service;

import com.gckavach.gckavachapp.alert.api.AlertCreateRequest;
import com.gckavach.gckavachapp.alert.api.AlertResponse;
import com.gckavach.gckavachapp.alert.domain.AlertSeverity;
import com.gckavach.gckavachapp.alert.domain.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Alert management.
 *
 * This interface defines the business operations supported
 * by the Alert Management module.
 *
 * Implementations are responsible for:
 *
 * 1. Creating alerts.
 * 2. Querying alerts.
 * 3. Acknowledging alerts.
 * 4. Resolving alerts.
 * 5. Suppressing alerts.
 */
public interface AlertService {

    /**
     * Creates a new alert.
     *
     * The implementation is responsible for:
     *
     * - Validating the request.
     * - Generating the alert fingerprint.
     * - Performing alert deduplication.
     * - Persisting the alert.
     * - Publishing the ALERT_CREATED event.
     *
     * @param request alert creation request
     * @return created alert response
     */
    AlertResponse createAlert(
            AlertCreateRequest request
    );

    /**
     * Queries alerts using optional filters.
     *
     * Supported filters:
     *
     * - status
     * - severity
     * - serviceName
     * - environment
     *
     * Pagination and sorting are controlled through Pageable.
     *
     * @param status alert status filter
     * @param severity alert severity filter
     * @param serviceName service name filter
     * @param environment environment filter
     * @param pageable pagination and sorting information
     * @return paginated alert responses
     */
    Page<AlertResponse> queryAlerts(
            AlertStatus status,
            AlertSeverity severity,
            String serviceName,
            String environment,
            Pageable pageable
    );

    AlertResponse getAlert(String alertId);

    /**
     * Acknowledges an alert.
     *
     * Expected lifecycle transition:
     *
     * OPEN -> ACKNOWLEDGED
     *
     * The implementation should keep the deduplication
     * fingerprint active because ACKNOWLEDGED is still
     * considered an active alert.
     *
     * @param alertId alert identifier
     * @return acknowledged alert response
     */
    AlertResponse acknowledgeAlert(
            String alertId
    );

    /**
     * Resolves an alert.
     *
     * Expected lifecycle transitions:
     *
     * OPEN -> RESOLVED
     *
     * ACKNOWLEDGED -> RESOLVED
     *
     * Once resolved, the implementation should release
     * the alert's deduplication fingerprint.
     *
     * @param alertId alert identifier
     * @return resolved alert response
     */
    AlertResponse resolveAlert(
            String alertId
    );

    /**
     * Suppresses an alert.
     *
     * Expected lifecycle transitions:
     *
     * OPEN -> SUPPRESSED
     *
     * ACKNOWLEDGED -> SUPPRESSED
     *
     * Once suppressed, the implementation should release
     * the alert's deduplication fingerprint.
     *
     * @param alertId alert identifier
     * @return suppressed alert response
     */
    AlertResponse suppressAlert(
            String alertId
    );
}