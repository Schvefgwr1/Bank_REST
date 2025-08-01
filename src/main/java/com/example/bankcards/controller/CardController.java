package com.example.bankcards.controller;

import com.example.bankcards.dto.api.cards.*;
import com.example.bankcards.exception.custom_exceptions.*;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Cards", description = "API для управления банковскими картами")
@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @Operation(
            summary = "Создать новую карту",
            description = "Создает банковскую карту по данным из запроса",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Карта успешно создана",
                            content = @Content(schema = @Schema(implementation = CardResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или статус карты",
                            content = @Content),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content),
                    @ApiResponse(responseCode = "500", description = "Ошибка статуса",
                            content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest req)
    throws CardStatusException, UserException, UpdateEntityException {
        CardResponse response = cardService.createCard(req);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Получить все карты",
            description = "Возвращает список всех банковских карт с пагинацией",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список карт",
                            content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных",
                            content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<Page<GetAllCardsResponse>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws DatabaseException {
        Pageable pageable = PageRequest.of(page, size);
        Page<GetAllCardsResponse> response = cardService.getAllCards(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Получить карты текущего пользователя",
            description = "Возвращает список карт пользователя, идентифицированного по токену, с пагинацией",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список карт пользователя",
                            content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка контекста",
                            content = @Content),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных",
                            content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/my")
    public ResponseEntity<Page<CardResponse>> getMyCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) throws DatabaseException, ContextException, UserException {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new ContextException("Can't find", "userID");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<CardResponse> response = cardService.getMyCards(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Обновить карту по ID",
            description = "Обновляет информацию по карте с указанным ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Карта успешно обновлена",
                            content = @Content(schema = @Schema(implementation = CardResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка обновления или статус карты",
                            content = @Content),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена",
                            content = @Content),
                    @ApiResponse(responseCode = "500", description = "Ошибка статуса или базы данных",
                            content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{id}")
    public ResponseEntity<CardResponse> updateCard(
            @PathVariable("id") Long cardID,
            @Valid @RequestBody UpdateCardRequest request
    ) throws CardException, CardStatusException, DatabaseException {
        return new ResponseEntity<>(cardService.updateCard(cardID, request), HttpStatus.OK);
    }

    @Operation(
            summary = "Удалить карту по ID",
            description = "Удаляет карту с указанным ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Карта успешно удалена",
                            content = @Content(schema = @Schema(implementation = DeleteCardResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Ошибка авторизации", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена",
                            content = @Content),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных",
                            content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteCardResponse> deleteCard(@PathVariable("id") Long id)
    throws CardException, DatabaseException {
        return new ResponseEntity<>(cardService.deleteCard(id), HttpStatus.OK);
    }
}
