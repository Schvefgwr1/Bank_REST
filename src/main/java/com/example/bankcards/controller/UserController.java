package com.example.bankcards.controller;

import com.example.bankcards.dto.api.users.DeleteUserResponse;
import com.example.bankcards.dto.api.users.UpdateUserRequest;
import com.example.bankcards.dto.api.users.UserRequest;
import com.example.bankcards.dto.api.users.UserResponse;
import com.example.bankcards.exception.custom_exceptions.DatabaseException;
import com.example.bankcards.exception.custom_exceptions.RoleException;
import com.example.bankcards.exception.custom_exceptions.UpdateEntityException;
import com.example.bankcards.exception.custom_exceptions.UserException;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Users", description = "API для управления пользователями")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Создать нового пользователя",
            description = "Создает пользователя с заданными данными",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно создан",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или при обновлении",
                            content = @Content),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных или роли",
                            content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest req)
    throws RoleException, DatabaseException, UpdateEntityException {
        return new ResponseEntity<>(userService.createUser(req), HttpStatus.OK);
    }

    @Operation(
            summary = "Получить список пользователей",
            description = "Возвращает список пользователей с пагинацией",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список пользователей",
                            content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных",
                            content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws DatabaseException {
        Pageable pageable = PageRequest.of(page, size);
        return new ResponseEntity<>(userService.getUsers(pageable), HttpStatus.OK);
    }

    @Operation(
            summary = "Обновить пользователя по ID",
            description = "Обновляет данные пользователя с указанным ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или при обновлении",
                            content = @Content),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных или роли",
                            content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UpdateUserRequest req, @PathVariable("id") Long userId)
    throws RoleException, DatabaseException, UpdateEntityException, UserException {
        return new ResponseEntity<>(userService.updateUser(req, userId), HttpStatus.OK);
    }

    @Operation(
            summary = "Удалить пользователя по ID",
            description = "Удаляет пользователя с указанным ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно удалён",
                            content = @Content(schema = @Schema(implementation = DeleteUserResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных",
                            content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteUserResponse> deleteUser(@PathVariable("id") Long userId)
    throws UserException, DatabaseException {
        return new ResponseEntity<>(userService.deleteUser(userId), HttpStatus.OK);
    }
}
