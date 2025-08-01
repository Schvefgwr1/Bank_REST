package com.example.bankcards.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
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
        String resourceLocation = keyPath.startsWith("classpath:")
                ? keyPath.substring("classpath:".length())
                : keyPath;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceLocation)) {
            if (is == null) {
                throw new IOException("JWT key not found on classpath: " + resourceLocation);
            }
            byte[] keyBytes = is.readAllBytes();
            this.secret = Base64.getEncoder().encodeToString(keyBytes);
        }
    }
}

