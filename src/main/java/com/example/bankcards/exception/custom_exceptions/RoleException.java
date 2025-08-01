package com.example.bankcards.exception.custom_exceptions;

import lombok.Getter;

@Getter
public class RoleException extends Exception {
    private final String role;

    public RoleException(String role) {
        super("");
        this.role = role;
    }

    @Override
    public String getMessage() {
        return "Can't find role: " + role + " in DB";
    }
}

