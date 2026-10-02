package com.gckavach.gckavachapp.alert.correlation;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration for alert correlation.
 */
@ConfigurationProperties(prefix = "gckavacha.alert.correlation")
public class AlertCorrelationProperties {

    private Duration window = Duration.ofMinutes(10);

    public Duration getWindow() {
        return window;
    }

    public void setWindow(Duration window) {
        this.window = window;
    }
}