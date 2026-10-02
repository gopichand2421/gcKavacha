package com.gckavach.gckavachapp.common.exception;

import com.gckavach.gckavachapp.common.api.ApiErrorResponse;
import com.gckavach.gckavachapp.incident.controller.IncidentController;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Incident not found.
     */
    @ExceptionHandler(
            IncidentController.IncidentNotFoundException.class
    )
    public ResponseEntity<ApiErrorResponse> handleIncidentNotFound(
            IncidentController.IncidentNotFoundException ex) {
        log.warn(
                "Incident not found: {}",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(
                        "INCIDENT_NOT_FOUND",
                        ex.getMessage()
                ));
    }

    /**
     * Duplicate incident number.
     */
    @ExceptionHandler(IncidentAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleIncidentAlreadyExists(
            IncidentAlreadyExistsException ex) {

        log.warn(
                "Duplicate incident: {}",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ApiErrorResponse(
                                "INCIDENT_ALREADY_EXISTS",
                                ex.getMessage()
                        )
                );
    }

    /**
     * Bean validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .orElse("Validation failed");

        log.warn(
                "Request validation failed: {}",
                message
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new ApiErrorResponse(
                                "VALIDATION_ERROR",
                                message
                        )
                );
    }

    /**
     * Constraint validation errors.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {

        log.warn(
                "Constraint validation failed: {}",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new ApiErrorResponse(
                                "VALIDATION_ERROR",
                                ex.getMessage()
                        )
                );
    }

    /**
     * Illegal state caused by an invalid incident lifecycle transition.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
            IllegalStateException ex) {

        log.warn(
                "Invalid operation: {}",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ApiErrorResponse(
                                "INVALID_STATE",
                                ex.getMessage()
                        )
                );
    }
}