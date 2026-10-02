package com.gckavach.gckavachapp.alert.api;

import com.gckavach.gckavachapp.alert.domain.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlertCreateRequest(
        @NotBlank
        String externalAlertId,

        @NotBlank
        String title,

        String description,

        @NotBlank
        String source,

        @NotNull
        AlertSeverity severity,

        @NotBlank
        String serviceName,

        @NotBlank
        String environment
) {
}
