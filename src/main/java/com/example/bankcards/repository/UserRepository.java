package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByLogin(String username);

    boolean existsByLogin(@NotNull String login);
}
