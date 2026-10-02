package com.gckavach.gckavachapp.incident.api;

import com.gckavach.gckavachapp.incident.domain.IncidentSeverity;
import com.gckavach.gckavachapp.incident.domain.IncidentStatus;

/**
 * Search criteria used when querying incidents.
 *
 * <p>
 * All fields are optional. Only the fields supplied by the caller
 * are added to the MongoDB query.
 * </p>
 *
 * <p>
 * This approach prevents the repository from growing with a large
 * number of combinations such as:
 *
 * <ul>
 *     <li>status + severity</li>
 *     <li>status + severity + service</li>
 *     <li>status + severity + service + environment</li>
 *     <li>etc.</li>
 * </ul>
 */
public record IncidentSearchCriteria(

        /**
         * Incident lifecycle status.
         */
        IncidentStatus status,

        /**
         * Incident severity.
         */
        IncidentSeverity severity,

        /**
         * Service associated with the incident.
         */
        String serviceName,

        /**
         * Deployment environment.
         */
        String environment,

        /**
         * Incident number or partial incident number.
         */
        String incidentNumber,

        /**
         * Incident title or partial title.
         */
        String title,

        /**
         * User assigned to the incident.
         */
        String assignedTo
) {
}