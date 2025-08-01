package com.example.bankcards.dto.api.users;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Ответ с данными пользователя")
public class UserResponse {

    @Schema(description = "Идентификатор пользователя", example = "123")
    private final Long id;

    @Schema(description = "Логин пользователя", example = "user123")
    private final String login;

    @Schema(description = "Роль пользователя", example = "ADMIN")
    private final String role;
}
