package com.gckavach.gckavachapp.alert.service;

import com.gckavach.gckavachapp.alert.correlation.AlertCorrelation;

import java.util.List;

/**
 * Handles persistence of alert correlation relationships.
 */
public interface AlertCorrelationPersistenceService {

    AlertCorrelation save(AlertCorrelation correlation);

    List<AlertCorrelation> findByAlertId(String alertId);

    boolean exists(
            String primaryAlertId,
            String relatedAlertId
    );

    void deleteByAlertId(String alertId);
}