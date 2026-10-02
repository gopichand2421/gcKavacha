package com.gckavach.gckavachapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

import com.gckavach.gckavachapp.alert.event.AlertEvent;
import com.gckavach.gckavachapp.incident.event.IncidentEvent;

@Configuration
public class KafkaConsumerConfig {

    private final ConsumerFactory<String, IncidentEvent> incidentConsumerFactory;
    private final ConsumerFactory<String, AlertEvent> alertConsumerFactory;

    public KafkaConsumerConfig(
            ConsumerFactory<String, IncidentEvent> incidentConsumerFactory,
            ConsumerFactory<String, AlertEvent> alertConsumerFactory) {

        this.incidentConsumerFactory = incidentConsumerFactory;
        this.alertConsumerFactory = alertConsumerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, IncidentEvent>
    incidentEventKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, IncidentEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(incidentConsumerFactory);

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AlertEvent>
    alertEventKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, AlertEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(alertConsumerFactory);

        return factory;
    }
}