package com.gckavach.gckavachapp.incident.api;

import com.gckavach.gckavachapp.incident.domain.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentCreateRequest(

        @NotBlank(message = "Incident number is required")
        @Size(max = 50, message = "Incident number must not exceed 50 characters")
        String incidentNumber,

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Severity is required")
        IncidentSeverity severity,

        @NotBlank(message = "Service name is required")
        @Size(max = 100, message = "Service name must not exceed 100 characters")
        String serviceName,

        @NotBlank(message = "Environment is required")
        @Size(max = 50, message = "Environment must not exceed 50 characters")
        String environment
) { }