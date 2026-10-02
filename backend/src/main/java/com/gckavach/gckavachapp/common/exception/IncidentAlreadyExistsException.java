package com.gckavach.gckavachapp.common.exception;

/**
 * Thrown when an incident with the same incident number
 * already exists.
 */
public class IncidentAlreadyExistsException
        extends RuntimeException {

    public IncidentAlreadyExistsException(String message) {
        super(message);
    }

    public IncidentAlreadyExistsException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}