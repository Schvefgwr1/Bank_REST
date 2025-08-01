package com.example.bankcards.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        String base64Key = Files.readString(Paths.get(keyPath)).trim();
        this.secret = Base64.getDecoder().decode(base64Key);
    }
}
