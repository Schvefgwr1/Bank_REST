package com.example.bankcards.dto.api.cards;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Запрос на создание карты")
public class CreateCardRequest {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^\\d{4} \\d{4} \\d{4} \\d{4}$", message = "Card number must match pattern '**** **** **** ****'")
    @Schema(description = "Номер карты в формате '**** **** **** ****'", example = "1234 5678 9012 3456")
    private String cardNumber;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата окончания срока действия карты (будущая дата)", example = "2026-12-31", type = "string", format = "date")
    private LocalDate expiryDate;

    @DecimalMin(value = "0.01", message = "Balance must be greater than 0")
    @Schema(description = "Начальный баланс карты", example = "1000.00")
    private BigDecimal balance;

    @NotNull(message = "User ID is required")
    @Schema(description = "ID пользователя, которому принадлежит карта", example = "123")
    private Long userID;
}
