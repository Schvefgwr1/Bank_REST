package com.example.bankcards.dto.api.cards;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Ответ на удаление карты")
public class DeleteCardResponse {
    @Schema(description = "ID удалённой карты", example = "1")
    private final Long cardID;

    @Schema(description = "Сообщение об успешном удалении", example = "Card deleted successfully")
    private final String message;
}
