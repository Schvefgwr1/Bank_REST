package com.example.bankcards.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String keyPath;
    private String secret;

    @PostConstruct
    public void loadSecret() throws IOException {
        byte[] keyBytes = Files.readAllBytes(Paths.get(keyPath));
        this.secret = Base64.getEncoder().encodeToString(keyBytes);
    }
}
