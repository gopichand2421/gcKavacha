package com.gckavach.gckavachapp.alert.api;

import com.gckavach.gckavachapp.alert.domain.Alert;
import com.gckavach.gckavachapp.alert.domain.AlertSeverity;
import com.gckavach.gckavachapp.alert.domain.AlertStatus;

import java.time.Instant;

public record AlertResponse(
        String id,
        String externalAlertId,
        String title,
        String description,
        String source,
        AlertSeverity severity,
        AlertStatus status,
        String serviceName,
        String environment,
        String fingerprint,
        Instant startedAt,
        Instant acknowledgedAt,
        Instant resolvedAt,
        Instant createdAt
) {

    public static AlertResponse from(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getExternalAlertId(),
                alert.getTitle(),
                alert.getDescription(),
                alert.getSource(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getServiceName(),
                alert.getEnvironment(),
                alert.getFingerprint(),
                alert.getStartedAt(),
                alert.getAcknowledgedAt(),
                alert.getResolvedAt(),
                alert.getCreatedAt()
        );
    }
}
