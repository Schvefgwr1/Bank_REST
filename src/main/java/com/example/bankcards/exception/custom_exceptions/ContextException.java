package com.example.bankcards.exception.custom_exceptions;

public class ContextException extends Exception {
    private final String attributeContext;

    public ContextException(String message, String attributeContext) {
        super(message);
        this.attributeContext = attributeContext;
    }

    @Override
    public String getMessage() {
        return "Error of context with attribute: " + attributeContext + " and message: " + super.getMessage();
    }
}
