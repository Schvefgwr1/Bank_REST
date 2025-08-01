package com.example.bankcards.exception.handlers;

import com.example.bankcards.exception.custom_exceptions.ContextException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ContextExceptionHandler {

    @ExceptionHandler(ContextException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(ContextException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.toString()));
    }
}
