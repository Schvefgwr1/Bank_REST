package com.example.bankcards.dto.api.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Информация по карте с данными владельца")
public class GetAllCardsResponse {

    @Getter
    @Builder
    @Schema(description = "Информация о владельце карты")
    public static class OwnerInf {
        @Schema(description = "ID владельца карты", example = "123")
        private final Long id;

        @Schema(description = "Логин владельца карты", example = "user123")
        private final String login;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Информация о владельце карты")
    private final OwnerInf ownerInf;

    @Schema(description = "Информация о карте")
    private final CardResponse cardInf;
}
