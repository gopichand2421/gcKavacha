package com.gckavach.gckavachapp.alert.correlation;

import java.time.Instant;

/**
 * API response representing an alert correlation.
 */
public record AlertCorrelationResponse(
        String correlationId,
        String relatedAlertId,
        String correlationType,
        long timeDifferenceSeconds,
        Instant createdAt
) {

    public static AlertCorrelationResponse from(
            AlertCorrelation correlation) {

        return new AlertCorrelationResponse(
                correlation.getId(),
                correlation.getRelatedAlertId(),
                correlation.getCorrelationType(),
                correlation.getTimeDifferenceSeconds(),
                correlation.getCreatedAt()
        );
    }
}