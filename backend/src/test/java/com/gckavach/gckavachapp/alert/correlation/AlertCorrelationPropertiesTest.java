package com.gckavach.gckavachapp.alert.correlation;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlertCorrelationPropertiesTest {

    @Test
    void shouldUseTenMinuteDefaultWindow() {

        AlertCorrelationProperties properties =
                new AlertCorrelationProperties();

        assertEquals(
                Duration.ofMinutes(10),
                properties.getWindow()
        );
    }

    @Test
    void shouldAllowCustomWindow() {

        AlertCorrelationProperties properties =
                new AlertCorrelationProperties();

        properties.setWindow(
                Duration.ofMinutes(30)
        );

        assertEquals(
                Duration.ofMinutes(30),
                properties.getWindow()
        );
    }
}