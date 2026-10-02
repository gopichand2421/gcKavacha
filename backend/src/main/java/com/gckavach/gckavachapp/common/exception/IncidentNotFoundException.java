package com.gckavach.gckavachapp.common.exception;

/**
 * Thrown when an incident cannot be found.
 */
public class IncidentNotFoundException extends RuntimeException {

    public IncidentNotFoundException(String message) {
        super(message);
    }

    public IncidentNotFoundException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}