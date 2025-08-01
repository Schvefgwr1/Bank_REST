package com.example.bankcards.service;


import com.example.bankcards.dto.api.auth.RefreshTokenRequest;
import com.example.bankcards.dto.api.auth.SignInRequest;
import com.example.bankcards.dto.api.auth.SignInResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.util.JWTUtils;
import com.example.bankcards.security.OurUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;

@Service
@AllArgsConstructor
public class AuthService {

    private final OurUserDetailsService ourUserRepo;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public SignInResponse signIn(SignInRequest request) throws AuthenticationException, IllegalStateException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        var user = ourUserRepo.loadUserByUsername(request.getUsername());

        if (!(user instanceof User entityUser)) {
            throw new IllegalStateException("Expected user to be instance of User, but was: " + user.getClass());
        }
        var jwt = jwtUtils.generateToken(user, entityUser.getId());
        var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
        return SignInResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .expirationTime(Duration.ofHours(24))
                .build();
    }

    public SignInResponse refreshToken(RefreshTokenRequest refreshTokenRequest) throws AuthenticationException, IllegalStateException {
        String ourUsername = jwtUtils.extractUsername(refreshTokenRequest.getRefreshToken());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(ourUsername, refreshTokenRequest.getPassword()));
        UserDetails user = ourUserRepo.loadUserByUsername(ourUsername);
        if (jwtUtils.isTokenValid(refreshTokenRequest.getRefreshToken(), user)) {
            if (!(user instanceof User entityUser)) {
                throw new IllegalStateException("Expected user to be instance of User, but was: " + user.getClass());
            }
            var jwt = jwtUtils.generateToken(user, entityUser.getId());
            return SignInResponse.builder()
                    .token(jwt)
                    .refreshToken(refreshTokenRequest.getRefreshToken())
                    .expirationTime(Duration.ofHours(24))
                    .build();
        } else {
            throw new BadCredentialsException("Invalid token");
        }
    }
}