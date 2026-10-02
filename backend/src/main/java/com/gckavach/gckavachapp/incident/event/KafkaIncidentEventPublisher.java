package com.gckavach.gckavachapp.incident.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka-based implementation of {@link IncidentEventPublisher}.
 *
 * <p>This component converts the application-level event publishing
 * abstraction into a Kafka message.</p>
 */
@Component
public class KafkaIncidentEventPublisher
        implements IncidentEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaIncidentEventPublisher.class);

    /**
     * Kafka topic used for incident events.
     */
    public static final String INCIDENT_EVENTS_TOPIC =
            "gckavacha.incident.events";

    private final KafkaTemplate<String, IncidentEvent> kafkaTemplate;

    /**
     * Creates the Kafka incident event publisher.
     *
     * @param kafkaTemplate Kafka template configured for IncidentEvent
     */
    public KafkaIncidentEventPublisher(
            KafkaTemplate<String, IncidentEvent> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes an incident event to Kafka.
     *
     * <p>The incident ID is used as the Kafka key so that events belonging
     * to the same incident can remain ordered within a Kafka partition.</p>
     *
     * @param event incident event to publish
     */
    @Override
    public void publish(IncidentEvent event) {

        if (event == null) {
            log.warn("Ignoring null incident event");

            return;
        }

        log.info(
                "Publishing incident event: eventId={}, eventType={}, incidentId={}",
                event.eventId(),
                event.eventType(),
                event.incidentId()
        );

        kafkaTemplate.send(
                INCIDENT_EVENTS_TOPIC,
                event.incidentId(),
                event
        );

        log.debug(
                "Incident event submitted to Kafka: eventId={}, topic={}",
                event.eventId(),
                INCIDENT_EVENTS_TOPIC
        );
    }
}