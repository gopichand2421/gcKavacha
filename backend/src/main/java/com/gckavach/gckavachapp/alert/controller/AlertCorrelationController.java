package com.gckavach.gckavachapp.alert.controller;

import com.gckavach.gckavachapp.alert.correlation.AlertCorrelation;
import com.gckavach.gckavachapp.alert.correlation.AlertCorrelationResponse;
import com.gckavach.gckavachapp.alert.service.AlertCorrelationPersistenceService;
import com.gckavach.gckavachapp.alert.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for alert correlations.
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertCorrelationController {

    private final AlertCorrelationPersistenceService
            correlationPersistenceService;

    private final AlertService alertService;

    public AlertCorrelationController(
            AlertCorrelationPersistenceService correlationPersistenceService,
            AlertService alertService) {

        this.correlationPersistenceService =
                correlationPersistenceService;

        this.alertService = alertService;
    }

    /**
     * Returns all correlations associated with an alert.
     *
     * GET /api/alerts/{alertId}/correlations
     */
    @GetMapping("/{alertId}/correlations")
    public ResponseEntity<List<AlertCorrelationResponse>>
    getCorrelations(
            @PathVariable String alertId) {

        /*
         * Verify that the requested alert exists.
         *
         * AlertService throws AlertNotFoundException when
         * the alert does not exist.
         */
        alertService.getAlert(alertId);

        /*
         * Retrieve all correlation relationships involving
         * this alert.
         */
        List<AlertCorrelation> correlations =
                correlationPersistenceService
                        .findByAlertId(alertId);

        /*
         * Convert domain objects into API responses.
         */
        List<AlertCorrelationResponse> response =
                correlations.stream()
                        .map(correlation ->
                                toResponse(
                                        alertId,
                                        correlation
                                )
                        )
                        .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Converts a correlation domain object into an API response.
     *
     * The stored correlation is directional internally,
     * but the API presents it as a relationship from the
     * perspective of the requested alert.
     */
    private AlertCorrelationResponse toResponse(
            String requestedAlertId,
            AlertCorrelation correlation) {

        String relatedAlertId;

        if (requestedAlertId.equals(
                correlation.getPrimaryAlertId())) {

            relatedAlertId =
                    correlation.getRelatedAlertId();

        } else {

            relatedAlertId =
                    correlation.getPrimaryAlertId();
        }

        return new AlertCorrelationResponse(
                correlation.getId(),
                relatedAlertId,
                correlation.getCorrelationType(),
                correlation.getTimeDifferenceSeconds(),
                correlation.getCreatedAt()
        );
    }
}