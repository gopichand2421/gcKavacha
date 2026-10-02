package com.gckavach.gckavachapp.config;

import com.gckavach.gckavachapp.incident.event.KafkaIncidentEventPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic configuration for GcKavacha.
 *
 * <p>This configuration creates application topics when Kafka is configured
 * with topic auto-creation through Spring Kafka.</p>
 */
@Configuration
public class KafkaTopicConfig {

    /**
     * Topic containing alert domain events.
     */
    public static final String ALERT_EVENTS_TOPIC =
            "gckavacha.alert.events";

    /**
     * Creates the alert events topic.
     */
    @Bean
    public NewTopic alertEventsTopic() {

        return TopicBuilder
                .name(ALERT_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Creates the incident events topic.
     *
     * <p>Three partitions provide room for future horizontal scaling of
     * incident event consumers.</p>
     */
    @Bean
    public NewTopic incidentEventsTopic() {

        return TopicBuilder
                .name(KafkaIncidentEventPublisher.INCIDENT_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}