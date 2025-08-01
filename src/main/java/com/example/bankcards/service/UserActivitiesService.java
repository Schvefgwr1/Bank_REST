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
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UserActivitiesService {
    private final CardRepository cardRepository;
    private final CardTransferRepository cardTransferRepository;
    private final CardBlockRequestRepository blockRequestRepository;
    private final CardStatusRepository cardStatusRepository;

    @Transactional
    public CardTransfer transferMoney(Long userId, TransferRequest req)
    throws CardException, IllegalArgumentException, DatabaseException {
        try {
            Card fromCard = cardRepository.findByIdForUpdate(req.getFromCardId())
                    .orElseThrow(() -> new CardException(req.getFromCardId()));

            Card toCard = cardRepository.findByIdForUpdate(req.getToCardId())
                    .orElseThrow(() -> new CardException(req.getFromCardId()));

            if (!fromCard.getOwner().getId().equals(userId) || !toCard.getOwner().getId().equals(userId)) {
                throw new IllegalArgumentException("One or both cards do not belong to user");
            }

            if (fromCard.getBalance().compareTo(req.getAmount()) < 0) {
                throw new IllegalArgumentException("Insufficient funds");
            }

            fromCard.setBalance(fromCard.getBalance().subtract(req.getAmount()));
            toCard.setBalance(toCard.getBalance().add(req.getAmount()));

            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            return cardTransferRepository.save(CardTransfer.builder()
                    .fromCard(fromCard)
                    .toCard(toCard)
                    .amount(req.getAmount())
                    .transferTime(LocalDateTime.now())
                    .build()
            );
        } catch (PersistenceException | DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public Long requestBlockCard(Long userId, Long cardId) throws CardException, IllegalArgumentException {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardException(cardId));

        if (!card.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }

        CardBlockRequest request = CardBlockRequest.builder()
                .card(card)
                .status(CardBlockRequest.Status.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        return blockRequestRepository.save(request).getId();
    }

    public Page<CardBlockRequest> getActiveBlockRequests(Pageable pageable) throws DatabaseException {
        try {
            Page<CardBlockRequest> requests = blockRequestRepository.findByStatus(CardBlockRequest.Status.PENDING, pageable);
            requests.forEach(request -> {
                User owner = Hibernate.unproxy(request.getCard().getOwner(), User.class);
                request.getCard().setOwner(owner);
            });
            return requests;
        } catch (PersistenceException | DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void completeBlockRequest(Long requestId)
    throws BlockRequestException, DatabaseException, CardException, CardStatusException {
        try {
            CardBlockRequest request = blockRequestRepository.findById(requestId)
                    .orElseThrow(() -> new BlockRequestException(requestId));
            request.setStatus(CardBlockRequest.Status.COMPLETED);

            Card card = cardRepository.findById(request.getCard().getId())
                            .orElseThrow(() -> new CardException(request.getCard().getId()));
            CardStatus status = cardStatusRepository.findCardStatusByName(CardStatus.CardStatuses.BLOCKED)
                            .orElseThrow(() -> new CardStatusException(CardStatus.CardStatuses.BLOCKED));

            card.setStatus(status);
            cardRepository.save(card);
            blockRequestRepository.save(request);
        } catch (PersistenceException | DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
