package com.gckavach.gckavachapp.incident.event;


import com.gckavach.gckavachapp.incident.service.IncidentEventAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes incident events from Kafka and records them
 * in the incident event audit trail.
 *
 * <p>
 * This consumer is disabled during the "test" profile because
 * controller integration tests do not require a running Kafka broker.
 * </p>
 */
@Component
@Profile("!test")
public class IncidentEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(IncidentEventConsumer.class);

    private final IncidentEventAuditService auditService;

    public IncidentEventConsumer(
            IncidentEventAuditService auditService) {

        this.auditService = auditService;

        log.info("IncidentEventConsumer initialized: consumerGroup={}",
                "gckavacha-incident-consumer");
    }

    @KafkaListener(
            topics = "gckavacha.incident.events",
            groupId = "gckavacha-incident-consumer",
            containerFactory = "incidentEventKafkaListenerContainerFactory"
    )
    public void consume(IncidentEvent event) {

        if (event == null) {
            log.warn("Received null incident event");
            return;
        }

        log.debug(
                "Received incident event: eventId={}, eventType={}, incidentId={}",
                event.eventId(),
                event.eventType(),
                event.incidentId()
        );

        try {

            auditService.record(event);

            log.debug(
                    "Incident event audit recorded: eventId={}, incidentId={}",
                    event.eventId(),
                    event.incidentId()
            );

        } catch (RuntimeException exception) {

            log.error(
                    "Failed to process incident event: eventId={}, incidentId={}",
                    event.eventId(),
                    event.incidentId(),
                    exception
            );

            throw exception;
        }
    }
}
