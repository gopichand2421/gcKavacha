package com.gckavach.gckavachapp.alert.event;

import com.gckavach.gckavachapp.alert.domain.AlertSeverity;
import com.gckavach.gckavachapp.alert.domain.AlertStatus;

import java.time.Instant;

public record AlertEvent(
        String  eventId,
        AlertEventType eventType,
        Instant occurredAt,

        String alterId,
        String externalAlertId,

        String title,
        String source,

        AlertSeverity severity,
        AlertStatus status,

        String serviceName,
        String environment,

        String fingerprint
) { }
