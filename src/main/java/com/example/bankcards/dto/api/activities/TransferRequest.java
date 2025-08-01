package com.example.bankcards.dto.api.activities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TransferRequest {
    @NotNull(message = "From card ID is required")
    @Schema(description = "ID карты-отправителя", example = "123")
    private Long fromCardId;

    @NotNull(message = "To card ID is required")
    @Schema(description = "ID карты-получателя", example = "456")
    private Long toCardId;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Сумма перевода", example = "100.50")
    private BigDecimal amount;
}
