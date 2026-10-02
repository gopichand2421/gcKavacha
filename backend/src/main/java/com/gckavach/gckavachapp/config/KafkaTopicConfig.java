package com.gckavach.gckavachapp.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    public static final String ALERT_EVENTS_TOPIC = "gckavacha.alert.events";

    @Bean
    public NewTopic alertEventsTopic(){
        return new NewTopic(ALERT_EVENTS_TOPIC, 3, (short) 1);
    }
}
