package com.gckavach.gckavachapp.alert.event;


import com.gckavach.gckavachapp.alert.service.AlertEventAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for Alert events.
 *
 * <p>
 * This consumer receives alert lifecycle events from Kafka
 * and records them in the alert event audit trail.
 * </p>
 *
 * <p>
 * The consumer does not modify the Alert itself. MongoDB remains
 * the source of truth for the current Alert state, while this
 * consumer maintains the historical event/audit trail.
 * </p>
 */
@Component
public class AlertEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AlertEventConsumer.class
            );

    /**
     * Kafka consumer group for alert event processing.
     */
    private static final String CONSUMER_GROUP =
            "gckavacha-alert-consumer";

    private final AlertEventAuditService
            alertEventAuditService;

    /**
     * Creates the alert event consumer.
     *
     * @param alertEventAuditService service responsible
     *                               for persisting event audits
     */
    public AlertEventConsumer(
            AlertEventAuditService alertEventAuditService) {

        this.alertEventAuditService =
                alertEventAuditService;

        log.info(
                "AlertEventConsumer initialized: consumerGroup={}",
                CONSUMER_GROUP
        );
    }

    /**
     * Consumes alert events from Kafka.
     *
     * <p>
     * Events are persisted to the audit collection before
     * any event-specific processing is performed.
     * </p>
     *
     * @param event alert event received from Kafka
     */
    @KafkaListener(
            topics = KafkaAlertEventPublisher.ALERT_EVENTS_TOPIC,
            groupId = CONSUMER_GROUP,
            containerFactory =
                    "alertEventKafkaListenerContainerFactory"
    )
    public void consume(
            AlertEvent event) {

        if (event == null) {

            log.warn(
                    "Received null alert event"
            );

            return;
        }

        log.info(
                "Received alert event: eventId={}, eventType={}, alertId={}",
                event.eventId(),
                event.eventType(),
                event.alterId()
        );

        try {

            /*
             * Persist the event first.
             *
             * AlertEventAuditService is responsible for
             * handling duplicate event IDs so that Kafka
             * redelivery does not create duplicate audit records.
             */
            alertEventAuditService.record(event);

            /*
             * Perform event-specific processing.
             *
             * At this stage, audit persistence is the primary
             * responsibility. Additional processing can be added
             * here later for notifications, correlation, analytics,
             * or AI/RCA workflows.
             */
            processEvent(event);

            log.debug(
                    "Alert event processed successfully: eventId={}, eventType={}",
                    event.eventId(),
                    event.eventType()
            );

        } catch (Exception ex) {

            /*
             * Log the failure and rethrow the exception.
             *
             * Rethrowing allows the Kafka listener/container
             * error handling mechanism to determine whether the
             * message should be retried or sent to a dead-letter topic.
             */
            log.error(
                    "Failed to process alert event: eventId={}, eventType={}, alertId={}",
                    event.eventId(),
                    event.eventType(),
                    event.alterId(),
                    ex
            );

            throw ex;
        }
    }

    /**
     * Handles event-specific processing.
     *
     * <p>
     * The current implementation records audit events.
     * The switch provides an extension point for future
     * event-specific workflows.
     * </p>
     *
     * @param event received alert event
     */
    private void processEvent(
            AlertEvent event) {

        switch (event.eventType()) {

            case ALERT_CREATED -> {

                log.debug(
                        "Processing ALERT_CREATED: alertId={}",
                        event.alterId()
                );
            }

            case ALERT_ACKNOWLEDGED -> {

                log.debug(
                        "Processing ALERT_ACKNOWLEDGED: alertId={}",
                        event.alterId()
                );
            }

            case ALERT_RESOLVED -> {

                log.debug(
                        "Processing ALERT_RESOLVED: alertId={}",
                        event.alterId()
                );
            }

            case ALERT_SUPPRESSED -> {

                log.debug(
                        "Processing ALERT_SUPPRESSED: alertId={}",
                        event.alterId()
                );
            }

            default -> {

                log.warn(
                        "Unknown alert event type: eventId={}, eventType={}",
                        event.eventId(),
                        event.eventType()
                );
            }
        }
    }
}