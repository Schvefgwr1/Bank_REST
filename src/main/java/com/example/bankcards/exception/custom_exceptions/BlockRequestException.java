package com.example.bankcards.exception.custom_exceptions;

import lombok.Getter;

@Getter
public class BlockRequestException extends Exception {
    private final Long blockID;

    public BlockRequestException(Long blockID) {
        super("");
        this.blockID = blockID;
    }

    @Override
    public String getMessage() {
        return "Can't find block request with id: " + blockID + " in DB";
    }
}
