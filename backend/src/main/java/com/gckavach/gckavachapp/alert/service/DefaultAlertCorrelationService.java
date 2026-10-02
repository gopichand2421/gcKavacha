package com.gckavach.gckavachapp.alert.service;

import com.gckavach.gckavachapp.alert.correlation.AlertCorrelationCandidate;
import com.gckavach.gckavachapp.alert.correlation.AlertCorrelationResult;
import com.gckavach.gckavachapp.alert.domain.Alert;
import com.gckavach.gckavachapp.alert.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Default rule-based implementation of alert correlation.
 *
 * Correlation V1:
 *
 * 1. Same service
 * 2. Same environment
 * 3. Started within configured time window
 *
 * More advanced correlation rules can be introduced later.
 */
@Service
public class DefaultAlertCorrelationService
        implements AlertCorrelationService {

    private final AlertRepository alertRepository;

    public DefaultAlertCorrelationService(
            AlertRepository alertRepository) {

        this.alertRepository = alertRepository;
    }

    @Override
    public AlertCorrelationResult correlate(
            Alert alert,
            Duration window) {

        validate(alert, window);

        Instant alertTime = alert.getStartedAt();

        Instant from = alertTime.minus(window);
        Instant to = alertTime.plus(window);

        List<Alert> candidates =
                alertRepository
                        .findByServiceNameAndEnvironmentAndStartedAtBetween(
                                alert.getServiceName(),
                                alert.getEnvironment(),
                                from,
                                to
                        );

        List<AlertCorrelationCandidate> relatedAlerts =
                candidates.stream()
                        .filter(candidate ->
                                !candidate.getId().equals(alert.getId()))
                        .filter(candidate ->
                                isCorrelatable(candidate))
                        .map(candidate ->
                                new AlertCorrelationCandidate(
                                        candidate,
                                        calculateTimeDifference(
                                                alert,
                                                candidate
                                        )
                                ))
                        .sorted(
                                Comparator.comparing(
                                        AlertCorrelationCandidate::timeDifference
                                )
                        )
                        .toList();

        if (relatedAlerts.isEmpty()) {
            return AlertCorrelationResult.noCorrelaion();
        }

        Alert primaryAlert = determinePrimaryAlert(
                alert,
                relatedAlerts
        );

        return AlertCorrelationResult.correlated(
                primaryAlert,
                relatedAlerts
        );
    }

    private boolean isCorrelatable(Alert alert) {

        return alert.getStatus() != null;
    }

    private Duration calculateTimeDifference(
            Alert first,
            Alert second) {

        return Duration.between(
                first.getStartedAt(),
                second.getStartedAt()
        ).abs();
    }

    private Alert determinePrimaryAlert(
            Alert currentAlert,
            List<AlertCorrelationCandidate> relatedAlerts) {

        return relatedAlerts.stream()
                .map(AlertCorrelationCandidate::alert)
                .min(
                        Comparator.comparing(Alert::getStartedAt)
                )
                .orElse(currentAlert);
    }

    private void validate(
            Alert alert,
            Duration window) {

        if (alert == null) {
            throw new IllegalArgumentException(
                    "Alert is required"
            );
        }

        if (window == null || window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException(
                    "Correlation window must be greater than zero"
            );
        }

        if (alert.getStartedAt() == null) {
            throw new IllegalArgumentException(
                    "Alert startedAt is required"
            );
        }

        if (alert.getServiceName() == null
                || alert.getServiceName().isBlank()) {

            throw new IllegalArgumentException(
                    "Alert serviceName is required"
            );
        }

        if (alert.getEnvironment() == null
                || alert.getEnvironment().isBlank()) {

            throw new IllegalArgumentException(
                    "Alert environment is required"
            );
        }
    }
}