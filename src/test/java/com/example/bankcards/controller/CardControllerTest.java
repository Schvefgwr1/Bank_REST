package com.example.bankcards.controller;


import com.example.bankcards.dto.api.cards.*;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private CardController cardController;

    private ObjectMapper objectMapper;

    @Mock
    private CardService cardService;

    private CardResponse mockCardResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cardController).build();
        mockCardResponse = CardResponse.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(123.45))
                .expiryDate(LocalDate.of(2027, 1, 1))
                .maskNumber("**** **** **** 1234")
                .status(CardStatus.CardStatuses.ACTIVE)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateCard() throws Exception {
        CreateCardRequest request = new CreateCardRequest();
        request.setCardNumber("1234 5678 1234 5678");
        request.setExpiryDate(LocalDate.of(2027, 1, 1));
        request.setBalance(BigDecimal.valueOf(123.45));
        request.setUserID(100L);

        Mockito.when(cardService.createCard(any(CreateCardRequest.class)))
                .thenReturn(mockCardResponse);

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(mockCardResponse.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(123.45));
    }

    @Test
    void testGetAllCards() throws Exception {
        GetAllCardsResponse response = GetAllCardsResponse.builder()
                .cardInf(mockCardResponse)
                .ownerInf(null)
                .build();

        Page<GetAllCardsResponse> page = new PageImpl<>(List.of(response));

        Mockito.when(cardService.getAllCards(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardInf.id").value(mockCardResponse.getId()));
    }

    @Test
    void testUpdateCard() throws Exception {
        UpdateCardRequest request = UpdateCardRequest.builder()
                .status(CardStatus.CardStatuses.ACTIVE)
                .expiryDate(LocalDate.of(2028, 1, 1))
                .build();

        Mockito.when(cardService.updateCard(eq(1L), any(UpdateCardRequest.class)))
                .thenReturn(mockCardResponse);

        mockMvc.perform(put("/cards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockCardResponse.getId()));
    }

    @Test
    void testDeleteCard() throws Exception {
        DeleteCardResponse response = DeleteCardResponse.builder()
                .cardID(1L)
                .message("Successfully deleted")
                .build();

        Mockito.when(cardService.deleteCard(1L)).thenReturn(response);

        mockMvc.perform(delete("/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardID").value(1L))
                .andExpect(jsonPath("$.message").value("Successfully deleted"));
    }

    @Test
    void testGetMyCards() throws Exception {
        CardResponse card = mockCardResponse;
        Page<CardResponse> page = new PageImpl<>(List.of(card));

        Mockito.when(cardService.getMyCards(eq(5L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/cards/my")
                        .requestAttr("userId", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }
}
