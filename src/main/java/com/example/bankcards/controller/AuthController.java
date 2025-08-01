package com.example.bankcards.controller;

import com.example.bankcards.dto.api.auth.RefreshTokenRequest;
import com.example.bankcards.dto.api.auth.SignInRequest;
import com.example.bankcards.dto.api.auth.SignInResponse;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.AuthenticationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "API для входа и обновления доступа")
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private AuthService authService;

    @Operation(
            summary = "Вход пользователя",
            description = "Аутентифицирует пользователя и возвращает JWT-токен"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная авторизация",
                    content = @Content(schema = @Schema(implementation = SignInResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные", content = @Content),
            @ApiResponse(responseCode = "403", description = "Ошибка авторизации", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ошибка работы с пользователем", content = @Content)
    })
    @PostMapping("/signin")
    public ResponseEntity<SignInResponse> signIn(@RequestBody SignInRequest signInRequest)
    throws AuthenticationException, IllegalStateException {
        return new ResponseEntity<>(authService.signIn(signInRequest), HttpStatus.OK);
    }

    @Operation(
            summary = "Обновление JWT-токена",
            description = "Получение нового токена по refresh-токену",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Новый токен успешно выдан",
                    content = @Content(schema = @Schema(implementation = SignInResponse.class))),
            @ApiResponse(responseCode = "403", description = "Ошибка авторизации", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ошибка работы с пользователем", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<SignInResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest)
    throws AuthenticationException, IllegalStateException {
        return new ResponseEntity<>(authService.refreshToken(refreshTokenRequest), HttpStatus.OK);
    }
}