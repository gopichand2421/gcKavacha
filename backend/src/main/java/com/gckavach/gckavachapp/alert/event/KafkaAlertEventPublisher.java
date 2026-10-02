package com.gckavach.gckavachapp.alert.event;

import com.gckavach.gckavachapp.alert.domain.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka implementation of the AlertEventPublisher.
 *
 * <p>
 * This component converts an Alert domain object into
 * an AlertEvent and publishes it to the alert events topic.
 * </p>
 *
 * <p>
 * The publisher is intentionally separated from the
 * AlertService through the AlertEventPublisher interface.
 * This keeps the service layer decoupled from Kafka.
 * </p>
 */
@Component
public class KafkaAlertEventPublisher
        implements AlertEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(
                    KafkaAlertEventPublisher.class
            );

    /**
     * Kafka topic used for alert events.
     */
    public static final String ALERT_EVENTS_TOPIC =
            "gckavacha.alert.events";

    private final KafkaTemplate<String, AlertEvent>
            kafkaTemplate;

    /**
     * Creates the Kafka alert event publisher.
     *
     * @param kafkaTemplate Kafka template used to publish alert events
     */
    public KafkaAlertEventPublisher(
            KafkaTemplate<String, AlertEvent> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;

        log.info(
                "KafkaAlertEventPublisher initialized"
        );
    }

    /**
     * Publishes an alert event to Kafka.
     *
     * <p>
     * The alert ID is used as the Kafka message key.
     * This helps Kafka maintain ordering for events
     * belonging to the same alert within a partition.
     * </p>
     *
     * @param eventType type of alert event
     * @param alert alert snapshot
     */
    @Override
    public void publish(
            AlertEventType eventType,
            Alert alert) {

        /*
         * Create the event from the current
         * state of the alert.
         */
        AlertEvent event =
                new AlertEvent(
                        java.util.UUID.randomUUID().toString(),
                        eventType,
                        java.time.Instant.now(),
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

        log.info(
                "Publishing alert event: eventType={}, alertId={}, externalAlertId={}",
                eventType,
                alert.getId(),
                alert.getExternalAlertId()
        );

        /*
         * Publish the event asynchronously.
         *
         * Alert ID is used as the Kafka key so that
         * events for the same alert are routed consistently.
         */
        kafkaTemplate
                .send(
                        ALERT_EVENTS_TOPIC,
                        alert.getId(),
                        event
                )
                .whenComplete(
                        (result, exception) -> {

                            /*
                             * Handle Kafka publishing failure.
                             */
                            if (exception != null) {

                                log.error(
                                        "Failed to publish alert event: eventType={}, alertId={}, eventId={}",
                                        eventType,
                                        alert.getId(),
                                        event.eventId(),
                                        exception
                                );

                                return;
                            }

                            /*
                             * Kafka successfully accepted
                             * the event.
                             */
                            log.debug(
                                    "Alert event published successfully: eventId={}, topic={}, partition={}, offset={}",
                                    event.eventId(),
                                    result.getRecordMetadata()
                                            .topic(),
                                    result.getRecordMetadata()
                                            .partition(),
                                    result.getRecordMetadata()
                                            .offset()
                            );
                        }
                );
    }
}