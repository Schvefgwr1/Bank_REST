package com.example.bankcards.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "encryption")
public class EncryptionProperties {
    private String keyPath;
    private byte[] secret;

    @PostConstruct
    public void loadSecret() throws IOException {
        String resourceLocation = keyPath.startsWith("classpath:")
                ? keyPath.substring("classpath:".length())
                : keyPath;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceLocation)) {
            if (is == null) {
                throw new IOException("Encryption key not found on classpath: " + resourceLocation);
            }
            String base64Key = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            this.secret = Base64.getDecoder().decode(base64Key);
        }
    }
}
