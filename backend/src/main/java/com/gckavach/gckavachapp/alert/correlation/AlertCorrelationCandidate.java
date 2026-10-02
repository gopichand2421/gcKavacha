package com.gckavach.gckavachapp.alert.correlation;

import com.gckavach.gckavachapp.alert.domain.Alert;

import java.time.Duration;

/**
 * Represents an alert that may be related
 * to the current alert.
 */
public record AlertCorrelationCandidate(
        Alert alert,
        Duration timeDifference
) { }
