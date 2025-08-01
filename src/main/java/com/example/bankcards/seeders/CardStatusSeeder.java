package com.example.bankcards.seeders;


import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.CardStatus.CardStatuses;
import com.example.bankcards.repository.CardStatusRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CardStatusSeeder {

    private final CardStatusRepository cardStatusRepository;

    @PostConstruct
    public void seedCardStatuses() {
        Arrays.stream(CardStatuses.values()).forEach(statusEnum -> {
            boolean exists = cardStatusRepository.existsByName(statusEnum);
            if (!exists) {
                cardStatusRepository.save(new CardStatus(null, statusEnum));
            }
        });
    }
}

