package com.paytaxihub.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Every error answers with a short, safe {"message": ...}. Internal details are logged, never returned. */
@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private static ResponseEntity<Map<String, Object>> body(HttpStatus status, String error, String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("timestamp", LocalDateTime.now());
        m.put("status", status.value());
        m.put("error", error);
        m.put("message", message);
        return ResponseEntity.status(status).body(m);
    }

    @ExceptionHandler(SecurityException.class)
    ResponseEntity<Map<String, Object>> security(SecurityException e) {
        return body(HttpStatus.FORBIDDEN, "Forbidden", e.getMessage() == null ? "You do not have permission to perform this action." : e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> bad(IllegalArgumentException e) {
        return body(HttpStatus.BAD_REQUEST, "Validation error", e.getMessage() == null ? "Invalid request." : e.getMessage());
    }

    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class})
    ResponseEntity<Map<String, Object>> malformed(Exception e) {
        return body(HttpStatus.BAD_REQUEST, "Bad request", "The request could not be understood. Please check your details and try again.");
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    ResponseEntity<Map<String, Object>> missing(java.util.NoSuchElementException e) {
        return body(HttpStatus.NOT_FOUND, "Not found", "The requested record was not found.");
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Map<String, Object>> method(Exception e) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", "That action is not supported.");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> general(Exception e) {
        LOG.error("Unhandled server error", e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", "Something went wrong on our side. Please try again.");
    }
}
