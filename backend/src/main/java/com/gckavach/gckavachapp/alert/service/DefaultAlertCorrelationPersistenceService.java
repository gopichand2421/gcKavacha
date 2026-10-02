package com.gckavach.gckavachapp.alert.service;

import com.gckavach.gckavachapp.alert.correlation.AlertCorrelation;
import com.gckavach.gckavachapp.alert.repository.AlertCorrelationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default MongoDB implementation of alert correlation persistence.
 */
@Service
public class DefaultAlertCorrelationPersistenceService
        implements AlertCorrelationPersistenceService {

    private final AlertCorrelationRepository repository;

    public DefaultAlertCorrelationPersistenceService(
            AlertCorrelationRepository repository) {

        this.repository = repository;
    }

    @Override
    public AlertCorrelation save(
            AlertCorrelation correlation) {
        validate(correlation);

        AlertCorrelation normalized =
                normalize(correlation);

        return repository.save(normalized);
    }

    @Override
    public List<AlertCorrelation> findByAlertId(
            String alertId) {

        validateAlertId(alertId);

        return repository
                .findByPrimaryAlertIdOrRelatedAlertId(
                        alertId,
                        alertId
                );
    }

    @Override
    public boolean exists(
            String primaryAlertId,
            String relatedAlertId) {

        validateAlertId(primaryAlertId);
        validateAlertId(relatedAlertId);

        return repository
                .existsByPrimaryAlertIdAndRelatedAlertId(
                        primaryAlertId,
                        relatedAlertId
                );
    }

    @Override
    public void deleteByAlertId(
            String alertId) {

        validateAlertId(alertId);

        repository.deleteByPrimaryAlertId(alertId);
        repository.deleteByRelatedAlertId(alertId);
    }

    private void validate(
            AlertCorrelation correlation) {

        if (correlation == null) {
            throw new IllegalArgumentException(
                    "Correlation is required"
            );
        }

        validateAlertId(
                correlation.getPrimaryAlertId()
        );

        validateAlertId(
                correlation.getRelatedAlertId()
        );

        if (correlation.getPrimaryAlertId()
                .equals(correlation.getRelatedAlertId())) {

            throw new IllegalArgumentException(
                    "An alert cannot be correlated with itself"
            );
        }
    }

    private void validateAlertId(
            String alertId) {

        if (alertId == null || alertId.isBlank()) {
            throw new IllegalArgumentException(
                    "Alert ID is required"
            );
        }
    }

    private AlertCorrelation normalize(
            AlertCorrelation correlation) {

        String first =
                correlation.getPrimaryAlertId();

        String second =
                correlation.getRelatedAlertId();

        if (first.compareTo(second) < 0) {
            return correlation;
        }

        return new AlertCorrelation(
                second,
                first,
                correlation.getCorrelationType(),
                correlation.getTimeDifferenceSeconds()
        );
    }
}