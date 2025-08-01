package com.example.bankcards.entity;

import com.example.bankcards.util.CardNumberEncryptor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, unique = true)
    @Convert(converter = CardNumberEncryptor.class)
    @JsonIgnore
    private String encryptedCardNumber;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private CardStatus status;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User owner;

    // Возвращает маскированный номер для отображения
    public String getMaskedCardNumber() {
        if (encryptedCardNumber == null || encryptedCardNumber.length() < 4) return "****";
        return "**** **** **** " + encryptedCardNumber.substring(encryptedCardNumber.length() - 4);
    }
}

