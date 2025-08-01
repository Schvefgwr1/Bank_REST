package com.example.bankcards.dto.api.cards;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "Ответ с информацией по карте")
public class CardResponse {
    @Schema(description = "Уникальный идентификатор карты", example = "1")
    private final Long id;

    @Schema(description = "Замаскированный номер карты, например '**** **** **** 3456'", example = "**** **** **** 3456")
    private final String maskNumber;

    @Schema(description = "Дата окончания срока действия карты", example = "2026-12-31", type = "string", format = "date")
    private final LocalDate expiryDate;

    @Schema(description = "Текущий баланс карты", example = "1500.50")
    private final BigDecimal balance;

    @Schema(description = "Статус карты", example = "ACTIVE")
    private final CardStatus.CardStatuses status;
}
