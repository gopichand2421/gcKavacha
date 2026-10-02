package com.gckavach.gckavachapp.incident.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IncidentAssignRequest (
        @NotBlank(message = "Assigned user is required")
        @Size(
                max = 100,
                message = "Assigned user must not exceed 100 characters"
        )
        String assignedTo
){
}
