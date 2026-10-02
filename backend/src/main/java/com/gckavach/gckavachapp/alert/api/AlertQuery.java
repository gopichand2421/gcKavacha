package com.gckavach.gckavachapp.alert.api;

import com.gckavach.gckavachapp.alert.domain.AlertSeverity;
import com.gckavach.gckavachapp.alert.domain.AlertStatus;

public record AlertQuery(
        AlertStatus status,
        AlertSeverity severity,
        String serviceName,
        String environment
) {
}