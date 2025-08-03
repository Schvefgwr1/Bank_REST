package com.example.bankcards.service;


import com.example.bankcards.dto.api.activities.TransferRequest;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.custom_exceptions.BlockRequestException;
import com.example.bankcards.exception.custom_exceptions.CardException;
import com.example.bankcards.exception.custom_exceptions.CardStatusException;
import com.example.bankcards.exception.custom_exceptions.DatabaseException;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardStatusRepository;
import com.example.bankcards.repository.CardTransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserActivitiesServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock private CardTransferRepository cardTransferRepository;
    @Mock private CardBlockRequestRepository blockRequestRepository;
    @Mock private CardStatusRepository cardStatusRepository;

    @InjectMocks
    private UserActivitiesService userActivitiesService;

    private User testUser;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        fromCard = new Card();
        fromCard.setId(100L);
        fromCard.setBalance(BigDecimal.valueOf(500));
        fromCard.setOwner(testUser);

        toCard = new Card();
        toCard.setId(200L);
        toCard.setBalance(BigDecimal.valueOf(300));
        toCard.setOwner(testUser);
    }

    // transferMoney tests

    @Test
    void transferMoney_success() throws CardException {
        TransferRequest req = createTransferRequest();

        when(cardRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cardTransferRepository.save(any(CardTransfer.class))).thenAnswer(inv -> inv.getArgument(0));

        CardTransfer transfer = userActivitiesService.transferMoney(1L, req);

        assertEquals(req.getAmount(), transfer.getAmount());
        assertEquals(fromCard, transfer.getFromCard());
        assertEquals(toCard, transfer.getToCard());
    }

    @Test
    void transferMoney_cardNotFound_shouldThrowCardException() {
        TransferRequest req = createTransferRequest();
        when(cardRepository.findByIdForUpdate(100L)).thenReturn(Optional.empty());

        assertThrows(CardException.class, () -> userActivitiesService.transferMoney(1L, req));
    }

    @Test
    void transferMoney_wrongOwner_shouldThrowIllegalArgumentException() {
        TransferRequest req = createTransferRequest();
        fromCard.getOwner().setId(2L);

        when(cardRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(toCard));

        assertThrows(IllegalArgumentException.class, () -> userActivitiesService.transferMoney(1L, req));
    }

    @Test
    void transferMoney_insufficientFunds_shouldThrowIllegalArgumentException() {
        TransferRequest req = createTransferRequest();
        fromCard.setBalance(BigDecimal.valueOf(5));

        when(cardRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(toCard));

        assertThrows(IllegalArgumentException.class, () -> userActivitiesService.transferMoney(1L, req));
    }

    @Test
    void transferMoney_dataAccessException_shouldThrowDatabaseException() {
        TransferRequest req = createTransferRequest();

        when(cardRepository.findByIdForUpdate(100L)).thenThrow(new DataAccessResourceFailureException("DB"));

        assertThrows(DatabaseException.class, () -> userActivitiesService.transferMoney(1L, req));
    }

    // requestBlockCard tests

    @Test
    void requestBlockCard_success() throws CardException {
        when(cardRepository.findById(100L)).thenReturn(Optional.of(fromCard));
        when(blockRequestRepository.save(any())).thenReturn(CardBlockRequest.builder().id(999L).build());

        Long id = userActivitiesService.requestBlockCard(1L, 100L);

        assertEquals(999L, id);
    }

    @Test
    void requestBlockCard_wrongUser_shouldThrowIllegalArgumentException() {
        fromCard.getOwner().setId(2L);
        when(cardRepository.findById(100L)).thenReturn(Optional.of(fromCard));

        assertThrows(IllegalArgumentException.class, () -> userActivitiesService.requestBlockCard(1L, 100L));
    }

    @Test
    void requestBlockCard_cardNotFound_shouldThrowCardException() {
        when(cardRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(CardException.class, () -> userActivitiesService.requestBlockCard(1L, 100L));
    }

    // getActiveBlockRequests tests

    @Test
    void getActiveBlockRequests_success() {
        CardBlockRequest request = new CardBlockRequest();
        request.setCard(fromCard);
        request.setStatus(CardBlockRequest.Status.PENDING);

        Page<CardBlockRequest> page = new PageImpl<>(List.of(request));
        when(blockRequestRepository.findByStatus(eq(CardBlockRequest.Status.PENDING), any())).thenReturn(page);

        Page<CardBlockRequest> result = userActivitiesService.getActiveBlockRequests(Pageable.unpaged());

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getActiveBlockRequests_dbError_shouldThrowDatabaseException() {
        when(blockRequestRepository.findByStatus(any(), any())).thenThrow(new DataAccessResourceFailureException("DB"));

        assertThrows(DatabaseException.class, () -> userActivitiesService.getActiveBlockRequests(Pageable.unpaged()));
    }

    // completeBlockRequest tests

    @Test
    void completeBlockRequest_success() throws Exception {
        CardBlockRequest request = CardBlockRequest.builder()
                .id(1L).status(CardBlockRequest.Status.PENDING).card(fromCard).build();

        CardStatus blockedStatus = new CardStatus();
        blockedStatus.setName(CardStatus.CardStatuses.BLOCKED);

        when(blockRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(cardRepository.findById(100L)).thenReturn(Optional.of(fromCard));
        when(cardStatusRepository.findCardStatusByName(CardStatus.CardStatuses.BLOCKED))
                .thenReturn(Optional.of(blockedStatus));

        userActivitiesService.completeBlockRequest(1L);

        assertEquals(CardBlockRequest.Status.COMPLETED, request.getStatus());
        assertEquals(blockedStatus, fromCard.getStatus());
    }

    @Test
    void completeBlockRequest_blockRequestNotFound_shouldThrowBlockRequestException() {
        when(blockRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BlockRequestException.class, () -> userActivitiesService.completeBlockRequest(1L));
    }

    @Test
    void completeBlockRequest_cardNotFound_shouldThrowCardException() {
        CardBlockRequest request = CardBlockRequest.builder()
                .id(1L).status(CardBlockRequest.Status.PENDING).card(fromCard).build();

        when(blockRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(cardRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(CardException.class, () -> userActivitiesService.completeBlockRequest(1L));
    }

    @Test
    void completeBlockRequest_cardStatusNotFound_shouldThrowCardStatusException() {
        CardBlockRequest request = CardBlockRequest.builder()
                .id(1L).status(CardBlockRequest.Status.PENDING).card(fromCard).build();

        when(blockRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(cardRepository.findById(100L)).thenReturn(Optional.of(fromCard));
        when(cardStatusRepository.findCardStatusByName(CardStatus.CardStatuses.BLOCKED)).thenReturn(Optional.empty());

        assertThrows(CardStatusException.class, () -> userActivitiesService.completeBlockRequest(1L));
    }

    @Test
    void completeBlockRequest_dbError_shouldThrowDatabaseException() {
        when(blockRequestRepository.findById(1L)).thenThrow(new DataAccessResourceFailureException("DB"));

        assertThrows(DatabaseException.class, () -> userActivitiesService.completeBlockRequest(1L));
    }

    private TransferRequest createTransferRequest() {
        TransferRequest req = new TransferRequest();
        ReflectionTestUtils.setField(req, "fromCardId", 100L);
        ReflectionTestUtils.setField(req, "toCardId", 200L);
        ReflectionTestUtils.setField(req, "amount", BigDecimal.valueOf(100));
        return req;
    }
}
