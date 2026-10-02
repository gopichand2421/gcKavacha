package com.gckavach.gckavachapp.alert.service;

import com.gckavach.gckavachapp.alert.correlation.AlertCorrelationResult;
import com.gckavach.gckavachapp.alert.domain.Alert;

import java.time.Duration;

/**
 * Service responsible for identifying related alerts.
 */
public interface AlertCorrelationService {

    /**
     * Finds alerts that are potentially related
     * to the supplied alert.
     *
     * @param alert alert to correlate
     * @param window correlation time window
     * @return correlation result
     */
    AlertCorrelationResult correlate(
            Alert alert,
            Duration window
    );
}