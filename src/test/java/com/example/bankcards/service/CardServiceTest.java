package com.example.bankcards.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.bankcards.dto.api.cards.*;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.custom_exceptions.*;
import com.example.bankcards.repository.*;

import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardStatusRepository cardStatusRepository;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCard_success() throws Exception {
        Long userId = 1L;
        String cardNumber = "1234 5678 9012 3456";
        LocalDate expiryDate = LocalDate.now().plusYears(1);
        BigDecimal balance = BigDecimal.valueOf(1000);

        CreateCardRequest req = new CreateCardRequest();
        req.setUserID(userId);
        req.setCardNumber(cardNumber);
        req.setExpiryDate(expiryDate);
        req.setBalance(balance);

        User user = new User();
        user.setId(userId);

        CardStatus activeStatus = new CardStatus();
        activeStatus.setName(CardStatus.CardStatuses.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardStatusRepository.findCardStatusByName(CardStatus.CardStatuses.ACTIVE))
                .thenReturn(Optional.of(activeStatus));
        when(cardRepository.existsByEncryptedCardNumber(cardNumber)).thenReturn(false);

        Card savedCard = Card.builder()
                .id(100L)
                .encryptedCardNumber(cardNumber)
                .balance(balance)
                .expiryDate(expiryDate)
                .status(activeStatus)
                .owner(user)
                .build();

        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        CardResponse response = cardService.createCard(req);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("**** **** **** 3456", response.getMaskNumber());
        assertEquals(balance, response.getBalance());
        assertEquals(expiryDate, response.getExpiryDate());
        assertEquals(CardStatus.CardStatuses.ACTIVE, response.getStatus());

        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_userNotFound_throwsUserException() {
        Long userId = 1L;
        CreateCardRequest req = new CreateCardRequest();
        req.setUserID(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserException ex = assertThrows(UserException.class, () -> cardService.createCard(req));
        assertEquals(userId, ex.getUserID());
    }

    @Test
    void createCard_cardExists_throwsUpdateEntityException() throws Exception {
        Long userId = 1L;
        String cardNumber = "1234 5678 9012 3456";
        LocalDate expiryDate = LocalDate.now().plusYears(1);
        BigDecimal balance = BigDecimal.valueOf(1000);

        CreateCardRequest req = new CreateCardRequest();
        req.setUserID(userId);
        req.setCardNumber(cardNumber);
        req.setExpiryDate(expiryDate);
        req.setBalance(balance);

        User user = new User();
        user.setId(userId);

        CardStatus activeStatus = new CardStatus();
        activeStatus.setName(CardStatus.CardStatuses.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardStatusRepository.findCardStatusByName(CardStatus.CardStatuses.ACTIVE))
                .thenReturn(Optional.of(activeStatus));
        when(cardRepository.existsByEncryptedCardNumber(cardNumber)).thenReturn(true);

        UpdateEntityException ex = assertThrows(UpdateEntityException.class, () -> cardService.createCard(req));
        assertTrue(ex.getMessage().contains("**** **** **** 3456"));
    }

    @Test
    void getAllCards_shouldReturnMappedPage() throws DatabaseException {
        User user = new User();
        user.setId(1L);
        user.setLogin("testUser");

        CardStatus status = new CardStatus();
        status.setName(CardStatus.CardStatuses.ACTIVE);

        Card card = new Card();
        card.setId(100L);
        card.setOwner(user);
        card.setStatus(status);
        card.setBalance(new BigDecimal("200.00"));
        card.setExpiryDate(LocalDate.of(2026, 12, 31));
        card.setEncryptedCardNumber("1234 5678 1234 5678"); // for getMaskedCardNumber()

        List<Card> cards = List.of(card);
        Page<Card> page = new PageImpl<>(cards);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        when(cardRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<GetAllCardsResponse> result = cardService.getAllCards(pageable);

        // Then
        assertEquals(1, result.getTotalElements());

        GetAllCardsResponse response = result.getContent().get(0);
        assertEquals(100L, response.getCardInf().getId());
        assertEquals("**** **** **** 5678", response.getCardInf().getMaskNumber());
        assertEquals(new BigDecimal("200.00"), response.getCardInf().getBalance());
        assertEquals(LocalDate.of(2026, 12, 31), response.getCardInf().getExpiryDate());
        assertEquals(CardStatus.CardStatuses.ACTIVE, response.getCardInf().getStatus());

        assertNotNull(response.getOwnerInf());
        assertEquals(1L, response.getOwnerInf().getId());
        assertEquals("testUser", response.getOwnerInf().getLogin());
    }

    @Test
    void getAllCards_shouldThrowDatabaseExceptionOnFailure() {
        Pageable pageable = PageRequest.of(0, 10);
        when(cardRepository.findAll(pageable)).thenThrow(new RuntimeException("DB Error"));

        DatabaseException exception = assertThrows(DatabaseException.class, () -> cardService.getAllCards(pageable));

        assertTrue(exception.getMessage().contains("Error of db: DB Error"));
    }

    @Nested
    class GetMyCardsTests {
        private User user;

        @BeforeEach
        void setUp() {
            user = new User();
            user.setId(2L);
            user.setLogin("john_doe");
        }

        @Test
        void getMyCards_shouldReturnUserCardsMappedCorrectly() throws DatabaseException, UserException {
            CardStatus status = new CardStatus();
            status.setName(CardStatus.CardStatuses.BLOCKED);

            Card card1 = new Card();
            card1.setId(201L);
            card1.setOwner(user);
            card1.setStatus(status);
            card1.setBalance(new BigDecimal("150.00"));
            card1.setExpiryDate(LocalDate.of(2025, 5, 20));
            card1.setEncryptedCardNumber("1111 2222 3333 4444");

            Card card2 = new Card();
            card2.setId(202L);
            card2.setOwner(user);
            card2.setStatus(status);
            card2.setBalance(new BigDecimal("300.00"));
            card2.setExpiryDate(LocalDate.of(2026, 8, 15));
            card2.setEncryptedCardNumber("5555 6666 7777 8888");

            List<Card> cards = List.of(card1, card2);
            Pageable pageable = PageRequest.of(0, 5);
            Page<Card> page = new PageImpl<>(cards, pageable, cards.size());

            when(userRepository.existsById(user.getId())).thenReturn(true);
            when(cardRepository.findAllByOwner(user.getId(), pageable)).thenReturn(page);

            Page<CardResponse> result = cardService.getMyCards(user.getId(), pageable);

            assertEquals(2, result.getTotalElements());

            CardResponse first = result.getContent().get(0);
            assertEquals(201L, first.getId());
            assertEquals("**** **** **** 4444", first.getMaskNumber());
            assertEquals(new BigDecimal("150.00"), first.getBalance());
            assertEquals(LocalDate.of(2025, 5, 20), first.getExpiryDate());
            assertEquals(CardStatus.CardStatuses.BLOCKED, first.getStatus());

            CardResponse second = result.getContent().get(1);
            assertEquals(202L, second.getId());
            assertEquals("**** **** **** 8888", second.getMaskNumber());
        }

        @Test
        void getMyCards_shouldThrowDatabaseExceptionOnFailure() {
            Pageable pageable = PageRequest.of(0, 5);

            when(userRepository.existsById(user.getId())).thenReturn(true);
            when(cardRepository.findAllByOwner(user.getId(), pageable))
                    .thenThrow(new PersistenceException("DB Failure"));

            DatabaseException exception = assertThrows(DatabaseException.class, () -> cardService.getMyCards(user.getId(), pageable));

            assertTrue(exception.getMessage().contains("Error of DB: DB Failure"));
        }

        @Test
        void getMyCards_shouldThrowUserExceptionOnFailure() {
            Pageable pageable = PageRequest.of(0, 5);

            when(userRepository.existsById(user.getId())).thenReturn(false);

            UserException exception = assertThrows(UserException.class, () -> cardService.getMyCards(user.getId(), pageable));

            assertEquals(exception.getUserID(), user.getId());
        }
    }

    @Nested
    class UpdateCardTests {

        private Card card;
        private UpdateCardRequest request;
        private CardStatus newStatus;

        @BeforeEach
        void init() {
            card = new Card();
            card.setId(1L);
            card.setBalance(BigDecimal.valueOf(500));
            card.setExpiryDate(LocalDate.of(2025, 5, 20));

            CardStatus currentStatus = new CardStatus();
            currentStatus.setName(CardStatus.CardStatuses.ACTIVE);
            card.setStatus(currentStatus);

            newStatus = new CardStatus();
            newStatus.setName(CardStatus.CardStatuses.BLOCKED);

            request = UpdateCardRequest.builder().build();
        }

        @Test
        void updateCard_shouldUpdateStatusAndReturnResponse() throws Exception {
            request.setStatus(CardStatus.CardStatuses.BLOCKED);

            when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
            when(cardStatusRepository.findCardStatusByName(CardStatus.CardStatuses.BLOCKED))
                    .thenReturn(Optional.of(newStatus));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CardResponse response = cardService.updateCard(1L, request);

            assertEquals(1L, response.getId());
            assertEquals(CardStatus.CardStatuses.BLOCKED, response.getStatus());
        }

        @Test
        void updateCard_shouldUpdateExpiryDateIfNotBlocked() throws Exception {
            LocalDate newExpiry = LocalDate.of(2026, 1, 1);
            request.setExpiryDate(newExpiry);

            when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CardResponse response = cardService.updateCard(1L, request);

            assertEquals(newExpiry, response.getExpiryDate());
        }

        @Test
        void updateCard_shouldNotUpdateExpiryDateIfBlocked() throws Exception {
            card.getStatus().setName(CardStatus.CardStatuses.BLOCKED);
            request.setExpiryDate(LocalDate.of(2027, 1, 1));

            when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CardResponse response = cardService.updateCard(1L, request);

            assertEquals(LocalDate.of(2025, 5, 20), response.getExpiryDate()); // unchanged
        }

        @Test
        void updateCard_shouldThrowCardExceptionIfCardNotFound() {
            when(cardRepository.findById(99L)).thenReturn(Optional.empty());

            CardException ex = assertThrows(CardException.class, () -> cardService.updateCard(99L, request));

            assertTrue(ex.getMessage().contains("Can't find card with id"));
            assertEquals(ex.getCardID(), 99);
        }

        @Test
        void updateCard_shouldThrowCardStatusExceptionIfStatusNotFound() {
            request.setStatus(CardStatus.CardStatuses.BLOCKED);

            when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
            when(cardStatusRepository.findCardStatusByName(CardStatus.CardStatuses.BLOCKED)).thenReturn(Optional.empty());

            assertThrows(CardStatusException.class, () -> cardService.updateCard(1L, request));
        }

        @Test
        void updateCard_shouldThrowDatabaseExceptionOnPersistenceFailure() {
            when(cardRepository.findById(1L)).thenThrow(new DataAccessResourceFailureException("DB down"));

            assertThrows(DatabaseException.class, () -> cardService.updateCard(1L, request));
        }
    }

    @Nested
    class DeleteCardTests {
        @Test
        void deleteCard_shouldDeleteCardSuccessfully() throws CardException, DatabaseException {
            Long cardId = 1L;

            when(cardRepository.existsById(cardId)).thenReturn(true);
            doNothing().when(cardRepository).deleteById(cardId);

            DeleteCardResponse response = cardService.deleteCard(cardId);

            assertNotNull(response);
            assertEquals(cardId, response.getCardID());
            assertEquals("Successfully deleted", response.getMessage());

            verify(cardRepository).deleteById(cardId);
        }

        @Test
        void deleteCard_shouldThrowCardException_whenCardNotFound() {
            Long cardId = 2L;

            when(cardRepository.existsById(cardId)).thenReturn(false);

            assertThrows(CardException.class, () -> cardService.deleteCard(cardId));
        }

        @Test
        void deleteCard_shouldThrowDatabaseException_onRepositoryException() {
            Long cardId = 3L;

            when(cardRepository.existsById(cardId)).thenThrow(new PersistenceException("DB error"));

            assertThrows(DatabaseException.class, () -> cardService.deleteCard(cardId));
        }
    }
}
