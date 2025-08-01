package com.example.bankcards.exception.handlers;

import com.example.bankcards.exception.custom_exceptions.BlockRequestException;
import com.example.bankcards.exception.custom_exceptions.CardException;
import com.example.bankcards.exception.custom_exceptions.UpdateEntityException;
import com.example.bankcards.exception.custom_exceptions.UserException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler({SecurityException.class, AuthenticationException.class})
    public ResponseEntity<Map<String, String>> handleForbidden(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("Security error", ex.getMessage()));
    }

    @ExceptionHandler({UserException.class, CardException.class, BlockRequestException.class})
    public ResponseEntity<Map<String, String>> handleNotFoundErrors(Exception ex) {
        Map<String, String> res = new HashMap<>();
        res.put("error", ex.getMessage());

        if (ex instanceof UserException userEx) {
            res.put("userId", String.valueOf(userEx.getUserID()));
        } else if (ex instanceof CardException cardEx) {
            res.put("cardId", String.valueOf(cardEx.getCardID()));
        } else if (ex instanceof BlockRequestException blockEx) {
            res.put("blockRequestId", String.valueOf(blockEx.getBlockID()));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    @ExceptionHandler({UpdateEntityException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleUpdateErrors(Exception ex) {
        Map<String, String> res = new HashMap<>();
        res.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
