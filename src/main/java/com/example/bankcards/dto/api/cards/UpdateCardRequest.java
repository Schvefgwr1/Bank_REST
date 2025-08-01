package com.example.bankcards.dto.api.cards;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "Запрос на обновление информации по карте")
public class UpdateCardRequest {
    @Schema(description = "Статус карты", example = "ACTIVE")
    private final CardStatus.CardStatuses status;

    @Future(message = "Expiry date must be in the future")
    @Schema(description = "Дата окончания срока действия карты (будущая дата)", example = "2027-01-01", type = "string", format = "date")
    private final LocalDate expiryDate;
}
