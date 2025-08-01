package com.example.bankcards.controller;


import com.example.bankcards.dto.api.activities.TransferRequest;
import com.example.bankcards.dto.api.cards.DeleteCardResponse;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.CardTransfer;
import com.example.bankcards.exception.custom_exceptions.*;
import com.example.bankcards.service.UserActivitiesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Activities", description = "API для операций пользователей с банковскими картами")
@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class UserActivitiesController {
    private final UserActivitiesService userActivitiesService;

    @Operation(
            summary = "Перевод денег между картами",
            description = "Позволяет перевести деньги с одной карты пользователя на другую карту",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен",
                            content = @Content(schema = @Schema(implementation = CardTransfer.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Ошибка в запросе или параметрах",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных или сервиса",
                            content = @Content
                    )
            }
    )
    @PostMapping("/transfer")
    public ResponseEntity<CardTransfer> transferMoney(
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest
    ) throws ContextException, CardException, IllegalArgumentException, DatabaseException {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new ContextException("Can't find", "userID");
        }
        CardTransfer transfer = userActivitiesService.transferMoney(userId, request);
        transfer.getFromCard().setOwner(null);
        transfer.getToCard().setOwner(null);
        return new ResponseEntity<>(transfer, HttpStatus.OK);
    }

    @Operation(
            summary = "Запрос на блокировку карты",
            description = "Создает запрос на блокировку карты пользователя",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Запрос на блокировку успешно создан",
                        content = @Content(schema = @Schema(implementation = Long.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Ошибка в параметрах запроса",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервиса",
                            content = @Content
                    )
            }
    )
    @PostMapping("/block-request/{cardId}")
    public ResponseEntity<Long> requestBlockCard(
            @PathVariable Long cardId,
            HttpServletRequest httpRequest
    ) throws CardException, ContextException, IllegalArgumentException {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new ContextException("Can't find", "userID");
        }
        Long requestId = userActivitiesService.requestBlockCard(userId, cardId);
        return new ResponseEntity<>(requestId, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Получить активные запросы на блокировку карт",
            description = "Возвращает страницу с активными запросами на блокировку карт",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список активных запросов получен",
                            content = @Content(schema = @Schema(implementation = Page.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных",
                            content = @Content
                    )
            }
    )
    @GetMapping("/block-requests")
    public ResponseEntity<Page<CardBlockRequest>> getActiveBlockRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws DatabaseException {
        Pageable pageable = PageRequest.of(page, size);
        Page<CardBlockRequest> requests = userActivitiesService.getActiveBlockRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    @Operation(
            summary = "Завершить запрос на блокировку карты",
            description = "Отмечает запрос на блокировку карты как завершенный",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Запрос успешно завершен"),
                    @ApiResponse(responseCode = "400", description = "Ошибка в запросе",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "404", description = "Запрос не найден",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервиса",
                            content = @Content
                    )
            }
    )
    @PutMapping("/block-requests/{requestId}")
    public ResponseEntity<Void> completeBlockRequest(@PathVariable Long requestId)
    throws BlockRequestException, DatabaseException, CardStatusException, CardException {
        userActivitiesService.completeBlockRequest(requestId);
        return ResponseEntity.ok().build();
    }
}

