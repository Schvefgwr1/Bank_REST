package com.example.bankcards.repository;

import com.example.bankcards.entity.CardStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CardStatusRepository extends CrudRepository<CardStatus, Long> {
    Optional<CardStatus> findCardStatusByName(CardStatus.CardStatuses name);

    boolean existsByName(CardStatus.CardStatuses name);
}
