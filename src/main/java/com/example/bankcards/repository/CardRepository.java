package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    @Query(
            value = "SELECT * FROM cards WHERE owner_id = :ownerID",
            countQuery = "SELECT count(*) FROM cards WHERE owner_id = :ownerID",
            nativeQuery = true
    )
    Page<Card> findAllByOwner(@Param("ownerID") Long ownerID, Pageable pageable);

    boolean existsByEncryptedCardNumber(String encryptedCardNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id = :id")
    Optional<Card> findByIdForUpdate(@Param("id") Long id);
}

