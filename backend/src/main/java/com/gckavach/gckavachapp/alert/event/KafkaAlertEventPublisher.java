package com.gckavach.gckavachapp.alert.event;

import com.gckavach.gckavachapp.alert.domain.Alert;
import com.gckavach.gckavachapp.config.KafkaTopicConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class KafkaAlertEventPublisher implements AlertEventPublisher{

    private final KafkaTemplate<String, AlertEvent> kafkaTemplate;

    public KafkaAlertEventPublisher(KafkaTemplate<String, AlertEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(AlertEventType eventType, Alert alert) {

        AlertEvent event = new AlertEvent(
                UUID.randomUUID().toString(),
                eventType,
                Instant.now(),

                alert.getId(),
                alert.getExternalAlertId(),

                alert.getTitle(),
                alert.getSource(),

                alert.getSeverity(),
                alert.getStatus(),

                alert.getServiceName(),
                alert.getEnvironment(),

                alert.getFingerprint()
        );
        kafkaTemplate.send(KafkaTopicConfig.ALERT_EVENTS_TOPIC, alert.getId(), event);
    }
}
