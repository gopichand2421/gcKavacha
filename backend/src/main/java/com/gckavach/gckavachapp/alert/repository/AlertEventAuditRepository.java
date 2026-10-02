package com.gckavach.gckavachapp.alert.repository;

import com.gckavach.gckavachapp.alert.event.AlertEventAudit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AlertEventAuditRepository
        extends MongoRepository<AlertEventAudit, String> {

    Optional<AlertEventAudit> findByEventId(String eventId);

    List<AlertEventAudit> findByAlertIdOrderByOccurredAtAsc(
            String alertId
    );

    boolean existsByEventId(String eventId);
}
