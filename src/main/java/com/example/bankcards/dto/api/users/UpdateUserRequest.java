package com.example.bankcards.dto.api.users;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Запрос для обновления пользователя")
public class UpdateUserRequest {

    @Schema(description = "Логин пользователя", example = "user123")
    private final String login;

    @Schema(description = "Пароль пользователя", example = "secretPassword")
    private final String password;

    @Schema(description = "Роль пользователя", example = "ADMIN")
    private final String roleName;
}
