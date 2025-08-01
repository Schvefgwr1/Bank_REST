package com.example.bankcards.dto.api.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefreshTokenRequest {

    @Schema(description = "User password (required)", example = "password123")
    @NotBlank
    private String password;

    @Schema(description = "User username (required)", example = "john.doe")
    @NotBlank
    private String username;

    @Schema(description = "Refresh token issued earlier (required)", example = "eyJhbGciOiJIUzI1...")
    @NotBlank
    private String refreshToken;
}
