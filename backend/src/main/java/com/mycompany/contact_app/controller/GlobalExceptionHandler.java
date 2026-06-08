package com.mycompany.contact_app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handles illegal state transitions (e.g., ARCHIVED -> ACTIVE)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Handles database lookups that return empty (e.g., findById)
    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<Object> handleNotFound(java.util.NoSuchElementException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found");
    }

    // Catch-all for unexpected internal errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred");
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message);
        return new ResponseEntity<>(body, status);
    }
}