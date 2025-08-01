package com.example.bankcards.exception.custom_exceptions;

import lombok.Getter;

@Getter
public class CardException extends Exception {
    private final Long cardID;

    public CardException(Long cardID) {
        super("");
        this.cardID = cardID;
    }

    @Override
    public String getMessage() {
        return "Can't find card with id: " + cardID + " in DB";
    }
}
