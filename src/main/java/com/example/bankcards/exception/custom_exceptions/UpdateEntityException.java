package com.example.bankcards.exception.custom_exceptions;

public class UpdateEntityException extends Exception {
    private final String entityName;
    private final String paramName;
    private final String paramValue;

    public UpdateEntityException(String entityName, String paramName, String paramValue) {
        this.entityName = entityName;
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    @Override
    public String getMessage() {
        return "Entity: " + entityName + " can't be updated of parameter: " + paramName + " with not unique value: " + paramValue;
    }
}
