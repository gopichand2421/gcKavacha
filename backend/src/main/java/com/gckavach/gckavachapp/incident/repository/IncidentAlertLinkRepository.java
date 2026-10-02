package com.gckavach.gckavachapp.incident.repository;

import com.gckavach.gckavachapp.incident.domain.IncidentAlertLink;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentAlertLinkRepository
        extends MongoRepository<IncidentAlertLink, String> {

    Optional<IncidentAlertLink> findByIncidentIdAndAlertId(
            String incidentId,
            String alertId
    );

    List<IncidentAlertLink> findByIncidentIdOrderByCreatedAtAsc(
            String incidentId
    );

    List<IncidentAlertLink> findByAlertIdOrderByCreatedAtAsc(
            String alertId
    );

    boolean existsByIncidentIdAndAlertId(
            String incidentId,
            String alertId
    );

    void deleteByIncidentIdAndAlertId(
            String incidentId,
            String alertId
    );

    long countByIncidentId(String incidentId);

    long countByAlertId(String alertId);
}