package com.example.bankcards.exception.custom_exceptions;

import lombok.Getter;

@Getter
public class UserException extends Exception {
    private final Long userID;

    public UserException(Long userID) {
        super("");
        this.userID = userID;
    }

    @Override
    public String getMessage() {
        return "Can't find user with id: " + userID + " in DB";
    }
}
