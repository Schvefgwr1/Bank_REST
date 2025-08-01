package com.example.bankcards.dto.api.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignInRequest {

    @Schema(description = "User password", example = "password123")
    @NotBlank
    private final String password;

    @Schema(description = "User username", example = "john.doe")
    @NotBlank
    private final String username;
}
