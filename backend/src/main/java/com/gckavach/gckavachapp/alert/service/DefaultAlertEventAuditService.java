package com.gckavach.gckavachapp.alert.service;

import com.gckavach.gckavachapp.alert.event.AlertEvent;
import com.gckavach.gckavachapp.alert.event.AlertEventAudit;
import com.gckavach.gckavachapp.alert.repository.AlertEventAuditRepository;
import org.springframework.stereotype.Service;

@Service
public class DefaultAlertEventAuditService
        implements AlertEventAuditService {

    private final AlertEventAuditRepository repository;

    public DefaultAlertEventAuditService(
            AlertEventAuditRepository repository) {

        this.repository = repository;
    }

    @Override
    public void record(AlertEvent event) {

        if (repository.existsByEventId(event.eventId())) {
            return;
        }

        AlertEventAudit audit =
                new AlertEventAudit(event);

        repository.save(audit);
    }
}