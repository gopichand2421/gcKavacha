package com.gckavach.gckavachapp.alert.event;

import com.gckavach.gckavachapp.alert.domain.Alert;
import com.gckavach.gckavachapp.alert.domain.AlertSeverity;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class KafkaAlertEventPublisherTest {

    @Test
    void shouldPublishAlertEvent() {

        KafkaTemplate<String, AlertEvent> kafkaTemplate =
                org.mockito.Mockito.mock(KafkaTemplate.class);

        KafkaAlertEventPublisher publisher =
                new KafkaAlertEventPublisher(kafkaTemplate);

        Alert alert = new Alert(
                "EXT-001",
                "Payment latency high",
                "Payment API latency exceeded threshold",
                "PROMETHEUS",
                AlertSeverity.CRITICAL,
                "payment-service",
                "production",""
        );

        publisher.publish(
                AlertEventType.ALERT_CREATED,
                alert
        );

        verify(kafkaTemplate).send(
                eq("gckavacha.alert.events"),
                any(),
                any(AlertEvent.class)
        );
    }
}