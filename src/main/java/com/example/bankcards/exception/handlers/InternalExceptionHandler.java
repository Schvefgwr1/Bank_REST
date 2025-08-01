package com.example.bankcards.exception.handlers;

import com.example.bankcards.exception.custom_exceptions.CardStatusException;
import com.example.bankcards.exception.custom_exceptions.DatabaseException;
import com.example.bankcards.exception.custom_exceptions.RoleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class InternalExceptionHandler {

    @ExceptionHandler({
            CardStatusException.class,
            DatabaseException.class,
            IllegalStateException.class,
            RoleException.class
    })
    public ResponseEntity<Map<String, String>> handleInternalErrors(Exception ex) {
        return ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage()));
    }
}
