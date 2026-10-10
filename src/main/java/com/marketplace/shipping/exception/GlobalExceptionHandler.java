package com.marketplace.shipping.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handles map casting / type mismatch issues (e.g., Integer passed instead of Double)
    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<Object> handleClassCastException(ClassCastException ex, WebRequest request) {
        logger.error("Payload type conversion error", ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid field type in request payload", request);
    }

    // Handles null payload keys, missing timestamp parsing inputs, etc.
    @ExceptionHandler({NullPointerException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handleBadRequestExceptions(Exception ex, WebRequest request) {
        logger.error("Bad request error", ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage() != null ? ex.getMessage() : "Malformed request payload", request);
    }

    // Handles unhandled database connection/syntax errors
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDatabaseExceptions(DataAccessException ex, WebRequest request) {
        logger.error("Database error processing request", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database operational failure", request);
    }

    // Catch-all fallback for unhandled application errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unhandled application error", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, status);
    }
}