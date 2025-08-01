package com.example.bankcards.exception.custom_exceptions;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }

    public String getMessage() {
        return "Error of DB: " + super.getMessage();
    }
}
