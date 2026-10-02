package com.gckavach.gckavachapp.incident.repository;

import com.gckavach.gckavachapp.incident.domain.Incident;
import com.gckavach.gckavachapp.incident.domain.IncidentSeverity;
import com.gckavach.gckavachapp.incident.domain.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface IncidentRepository
        extends MongoRepository<Incident, String> {

    /**
     * Finds an incident using its human-readable incident number.
     */
    Optional<Incident> findByIncidentNumber(
            String incidentNumber
    );

    /**
     * Checks whether an incident number already exists.
     */
    boolean existsByIncidentNumber(
            String incidentNumber
    );

    /**
     * Finds incidents by status.
     */
    Page<Incident> findByStatus(
            IncidentStatus status,
            Pageable pageable
    );

    /**
     * Finds incidents by severity.
     */
    Page<Incident> findBySeverity(
            IncidentSeverity severity,
            Pageable pageable
    );

    /**
     * Finds incidents by service.
     */
    Page<Incident> findByServiceName(
            String serviceName,
            Pageable pageable
    );

    /**
     * Finds incidents by environment.
     */
    Page<Incident> findByEnvironment(
            String environment,
            Pageable pageable
    );

    /**
     * Finds incidents by status and severity.
     */
    Page<Incident> findByStatusAndSeverity(
            IncidentStatus status,
            IncidentSeverity severity,
            Pageable pageable
    );

    /**
     * Finds incidents by status and service.
     */
    Page<Incident> findByStatusAndServiceName(
            IncidentStatus status,
            String serviceName,
            Pageable pageable
    );

    /**
     * Finds incidents by status and environment.
     */
    Page<Incident> findByStatusAndEnvironment(
            IncidentStatus status,
            String environment,
            Pageable pageable
    );

    /**
     * Finds incidents by service and environment.
     */
    Page<Incident> findByServiceNameAndEnvironment(
            String serviceName,
            String environment,
            Pageable pageable
    );

    /**
     * Finds incidents by status, severity and service.
     */
    Page<Incident> findByStatusAndSeverityAndServiceName(
            IncidentStatus status,
            IncidentSeverity severity,
            String serviceName,
            Pageable pageable
    );

    /**
     * Finds incidents by status, severity,
     * service and environment.
     */
    Page<Incident>
    findByStatusAndSeverityAndServiceNameAndEnvironment(
            IncidentStatus status,
            IncidentSeverity severity,
            String serviceName,
            String environment,
            Pageable pageable
    );
}