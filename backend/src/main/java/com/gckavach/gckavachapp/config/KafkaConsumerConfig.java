package com.gckavach.gckavachapp.config;

import com.gckavach.gckavachapp.alert.event.AlertEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration.
 *
 * Responsible for configuring Kafka consumers used by
 * GcKavacha alert event listeners.
 */
@Configuration
public class KafkaConsumerConfig {

    /**
     * Kafka bootstrap server.
     */
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Creates Kafka ConsumerFactory for AlertEvent.
     *
     * @return configured consumer factory
     */
    @Bean
    public ConsumerFactory<String, AlertEvent>
    alertEventConsumerFactory() {

        Map<String, Object> properties =
                new HashMap<>();

        /*
         * Kafka broker address.
         */
        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        /*
         * Consumer group.
         */
        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "gckavacha-alert-consumer"
        );

        /*
         * Deserialize message keys as String.
         */
        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        /*
         * Deserialize AlertEvent JSON messages.
         */
        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JacksonJsonDeserializer.class
        );

        /*
         * Start from the earliest available message when
         * no committed offset exists.
         */
        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        /*
         * Let the listener container manage commits.
         */
        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        /*
         * Tell Jackson which Java class should be created
         * from the Kafka JSON payload.
         */
        properties.put(
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE,
                AlertEvent.class.getName()
        );

        /*
         * Allow deserialization of our AlertEvent package.
         */
        properties.put(
                JacksonJsonDeserializer.TRUSTED_PACKAGES,
                "com.gckavach.gckavachapp.alert.event"
        );

        return new DefaultKafkaConsumerFactory<>(
                properties
        );
    }

    /**
     * Creates the Kafka listener container factory.
     *
     * IMPORTANT:
     *
     * The bean name must match the name used by
     * AlertEventConsumer:
     *
     * alertEventKafkaListenerContainerFactory
     *
     * @return listener container factory
     */
    @Bean(name = "alertEventKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, AlertEvent>
    alertEventKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, AlertEvent>
                factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        /*
         * Use the AlertEvent consumer factory.
         */
        factory.setConsumerFactory(
                alertEventConsumerFactory()
        );

        /*
         * Start with one consumer thread.
         */
        factory.setConcurrency(1);

        return factory;
    }
}