package com.example.bankcards.dto.api.users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Запрос на создание пользователя")
public class UserRequest {

    @NotBlank(message = "Login must be required")
    @Schema(description = "Логин пользователя", example = "user123")
    private final String login;

    @NotBlank(message = "Password must be required")
    @Schema(description = "Пароль пользователя", example = "secretPassword")
    private final String password;

    @NotBlank(message = "Role's name must be required")
    @Schema(description = "Название роли пользователя", example = "USER")
    private final String roleName;
}
