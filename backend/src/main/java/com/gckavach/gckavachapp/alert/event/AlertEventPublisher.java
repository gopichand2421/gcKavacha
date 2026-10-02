package com.gckavach.gckavachapp.alert.event;

import com.gckavach.gckavachapp.alert.domain.Alert;

public interface AlertEventPublisher {

    void publish(AlertEventType eventType, Alert alert);
}
