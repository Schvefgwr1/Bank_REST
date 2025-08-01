package com.example.bankcards.exception.custom_exceptions;

import com.example.bankcards.entity.CardStatus;

public class CardStatusException extends Exception {
    private final CardStatus.CardStatuses status;

    public CardStatusException(CardStatus.CardStatuses status) {
        super("");
        this.status = status;
    }

    @Override
    public String getMessage() {
        return "Status: " + status + " doesn't exist in database";
    }
}
