package com.example.bankcards.repository;

import com.example.bankcards.entity.CardBlockRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CardBlockRequestRepository extends JpaRepository<CardBlockRequest, Long> {
    Page<CardBlockRequest> findByStatus(CardBlockRequest.Status status, Pageable pageable);
}