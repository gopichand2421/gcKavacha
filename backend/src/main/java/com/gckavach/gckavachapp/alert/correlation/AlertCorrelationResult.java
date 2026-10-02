package com.gckavach.gckavachapp.alert.correlation;

import com.gckavach.gckavachapp.alert.domain.Alert;

import java.util.List;

/**
 * Result of alert correlation.
 */
public record AlertCorrelationResult(
     boolean correlated,
     Alert primaryAlert,
     List<AlertCorrelationCandidate> relatedAlerts
) {

    public static AlertCorrelationResult noCorrelaion(){
        return new AlertCorrelationResult(false, null, List.of());
    }

    public static AlertCorrelationResult correlated( Alert primaryAlert,
                                                     List<AlertCorrelationCandidate> relatedAlerts){
        return new AlertCorrelationResult(
                true,
                primaryAlert,
                List.copyOf(relatedAlerts)
        );
    }
}
