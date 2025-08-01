package com.example.bankcards.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class OpenApiExporter {

    private static final String EXPORT_PATH = "docs/openapi.yaml";

    @EventListener(ApplicationReadyEvent.class)
    public void exportOpenApiSpec() {
        try {
            URL url = new URL("http://localhost:8080/v3/api-docs.yaml");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line).append("\n");
                }

                Path path = Paths.get(EXPORT_PATH);
                Files.createDirectories(path.getParent());
                Files.writeString(path, content.toString());

                log.info("OpenAPI YAML exported to: {}", path.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Failed to export OpenAPI YAML: {}", e.getMessage());
        }
    }
}
