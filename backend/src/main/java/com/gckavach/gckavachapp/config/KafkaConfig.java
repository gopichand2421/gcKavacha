package com.gckavach.gckavachapp.config;

import com.gckavach.gckavachapp.alert.event.AlertEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer configuration.
 *
 * Responsible for configuring KafkaTemplate used to publish
 * GcKavacha alert events.
 */
@Configuration
public class KafkaConfig {

    /**
     * Kafka bootstrap server.
     */
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Creates Kafka producer factory.
     *
     * @return configured producer factory
     */
    @Bean
    public ProducerFactory<String, AlertEvent> alertEventProducerFactory() {

        Map<String, Object> properties =
                new HashMap<>();

        /*
         * Kafka broker address.
         */
        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        /*
         * Serialize Kafka message keys as String.
         */
        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        /*
         * Serialize AlertEvent as JSON.
         */
        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(
                properties
        );
    }

    /**
     * Creates KafkaTemplate used by AlertEventPublisher.
     *
     * @return KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, AlertEvent> alertKafkaTemplate() {

        return new KafkaTemplate<>(
                alertEventProducerFactory()
        );
    }
}