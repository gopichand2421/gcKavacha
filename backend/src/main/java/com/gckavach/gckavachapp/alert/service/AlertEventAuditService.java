package com.gckavach.gckavachapp.alert.service;

import com.gckavach.gckavachapp.alert.event.AlertEvent;

public interface AlertEventAuditService {

    void record(AlertEvent event);
}
