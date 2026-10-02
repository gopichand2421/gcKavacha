package com.gckavach.gckavachapp.config;

import com.gckavach.gckavachapp.alert.event.AlertEvent;
import com.gckavach.gckavachapp.incident.event.IncidentEvent;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    /**
     * Common producer configuration.
     */
    private Map<String, Object> producerProperties() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getBootstrapServers()
        );

        properties.put(
                org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class
        );

        properties.put(
                org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        return properties;
    }

    /**
     * Producer factory for IncidentEvent.
     */
    @Bean
    public ProducerFactory<String, IncidentEvent> incidentProducerFactory() {

        return new DefaultKafkaProducerFactory<>(
                producerProperties()
        );
    }

    /**
     * KafkaTemplate for IncidentEvent.
     */
    @Bean
    public KafkaTemplate<String, IncidentEvent> incidentKafkaTemplate(
            ProducerFactory<String, IncidentEvent> incidentProducerFactory) {

        return new KafkaTemplate<>(incidentProducerFactory);
    }

    /**
     * Producer factory for AlertEvent.
     */
    @Bean
    public ProducerFactory<String, AlertEvent> alertProducerFactory() {

        return new DefaultKafkaProducerFactory<>(
                producerProperties()
        );
    }

    /**
     * KafkaTemplate for AlertEvent.
     */
    @Bean
    public KafkaTemplate<String, AlertEvent> alertKafkaTemplate(
            ProducerFactory<String, AlertEvent> alertProducerFactory) {

        return new KafkaTemplate<>(alertProducerFactory);
    }
}