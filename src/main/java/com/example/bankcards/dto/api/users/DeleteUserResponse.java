package com.example.bankcards.dto.api.users;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Ответ после удаления пользователя")
public class DeleteUserResponse {

    @Schema(description = "Идентификатор удалённого пользователя", example = "123")
    private final Long userID;

    @Schema(description = "Сообщение о результате удаления", example = "Пользователь успешно удалён")
    private final String message;
}
