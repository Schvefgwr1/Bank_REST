package com.example.bankcards.service;

import com.example.bankcards.dto.api.cards.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.custom_exceptions.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardStatusRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.PersistenceException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardStatusRepository cardStatusRepository;

    public CardResponse createCard(CreateCardRequest req)
    throws CardStatusException, DatabaseException, UserException, UpdateEntityException {
        Optional<User> userCont = userRepository.findById(req.getUserID());
        if(userCont.isPresent()) {
            CardStatus activeStatus = cardStatusRepository.findCardStatusByName(CardStatus.CardStatuses.ACTIVE)
                        .orElseThrow(() -> new CardStatusException(CardStatus.CardStatuses.ACTIVE));
            try {
                if (!cardRepository.existsByEncryptedCardNumber(req.getCardNumber())) {
                    Card card = cardRepository.save(Card.builder()
                            .encryptedCardNumber(req.getCardNumber())
                            .balance(req.getBalance())
                            .expiryDate(req.getExpiryDate())
                            .status(activeStatus)
                            .owner(userCont.get())
                            .build()
                    );
                    return CardResponse.builder()
                            .id(card.getId())
                            .maskNumber(card.getMaskedCardNumber())
                            .balance(card.getBalance())
                            .expiryDate(card.getExpiryDate())
                            .status(card.getStatus().getName())
                            .build();
                } else {
                    throw new UpdateEntityException(
                            "Card",
                            "number",
                            "**** **** **** " + req.getCardNumber().substring(req.getCardNumber().length() - 4)
                    );
                }
            } catch (UpdateEntityException e) {
                throw e;
            } catch (Exception e) {
                throw new DatabaseException(e.getMessage());
            }
        } else {
            throw new UserException(req.getUserID());
        }
    }

    public Page<GetAllCardsResponse> getAllCards(Pageable pageable) throws DatabaseException {
        try {
            Page<Card> page = cardRepository.findAll(pageable);
            return page.map(card -> {
                GetAllCardsResponse.OwnerInf ownerInf = GetAllCardsResponse.OwnerInf.builder()
                        .id(card.getOwner().getId())
                        .login(card.getOwner().getLogin())
                        .build();
                CardResponse cardInf = CardResponse.builder()
                        .id(card.getId())
                        .maskNumber(card.getMaskedCardNumber())
                        .expiryDate(card.getExpiryDate())
                        .balance(card.getBalance())
                        .status(card.getStatus().getName())
                        .build();
                return GetAllCardsResponse.builder()
                        .cardInf(cardInf)
                        .ownerInf(ownerInf)
                        .build();
            });
        } catch (Exception e) {
            throw new DatabaseException("Error of db: " + e.getMessage());
        }
    }


    public Page<CardResponse> getMyCards(Long userID, Pageable pageable) throws DatabaseException, UserException {
        try {
            if (userRepository.existsById(userID)) {
                Page<Card> page = cardRepository.findAllByOwner(userID, pageable);
                return page.map(card -> CardResponse.builder()
                        .id(card.getId())
                        .maskNumber(card.getMaskedCardNumber())
                        .expiryDate(card.getExpiryDate())
                        .balance(card.getBalance())
                        .status(card.getStatus().getName())
                        .build());
            } else {
                throw new UserException(userID);
            }
        } catch (PersistenceException | DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }


    public CardResponse updateCard(Long cardID, UpdateCardRequest req) throws
            CardException, CardStatusException, DatabaseException
    {
        try {
            Card card = cardRepository.findById(cardID)
                    .orElseThrow(() -> new CardException(cardID));
            if ((req.getStatus() != null) && (req.getStatus() != CardStatus.CardStatuses.EXPIRED)) {
                CardStatus status = cardStatusRepository.findCardStatusByName(req.getStatus())
                        .orElseThrow(() -> new CardStatusException(req.getStatus()));
                card.setStatus(status);
            }
            if ((req.getExpiryDate() != null) && (card.getStatus().getName() != CardStatus.CardStatuses.BLOCKED)) {
                card.setExpiryDate(req.getExpiryDate());
            }
            Card updatedCard = cardRepository.save(card);
            return CardResponse.builder()
                    .id(updatedCard.getId())
                    .balance(updatedCard.getBalance())
                    .status(updatedCard.getStatus().getName())
                    .maskNumber(updatedCard.getMaskedCardNumber())
                    .expiryDate(updatedCard.getExpiryDate())
                    .build();
        } catch (PersistenceException | DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public DeleteCardResponse deleteCard(Long cardId) throws CardException, DatabaseException {
        try {
            if (cardRepository.existsById(cardId)) {
                cardRepository.deleteById(cardId);
                return DeleteCardResponse.builder()
                        .cardID(cardId)
                        .message("Successfully deleted")
                        .build();
            } else {
                throw new CardException(cardId);
            }
        } catch (PersistenceException | DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
