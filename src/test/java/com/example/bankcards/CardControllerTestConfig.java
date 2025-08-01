package com.example.bankcards;


import com.example.bankcards.service.CardService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class CardControllerTestConfig {

    @Bean
    public CardService cardService() {
        return Mockito.mock(CardService.class);
    }
}
