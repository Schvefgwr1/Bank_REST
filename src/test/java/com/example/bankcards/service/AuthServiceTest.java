package com.example.bankcards.service;

import com.example.bankcards.dto.api.auth.RefreshTokenRequest;
import com.example.bankcards.dto.api.auth.SignInRequest;
import com.example.bankcards.dto.api.auth.SignInResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.OurUserDetailsService;
import com.example.bankcards.util.JWTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock private OurUserDetailsService userDetailsService;
    @Mock private JWTUtils jwtUtils;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private AuthService authService;

    private final User testUser = User.builder()
            .id(1L)
            .login("john.doe")
            .password("encodedPass")
            .role(Role.builder().id(1L).name("USER").permissions(List.of()).build())
            .build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void signIn_success() {
        SignInRequest request = new SignInRequest("password123", "john.doe");

        when(userDetailsService.loadUserByUsername("john.doe")).thenReturn(testUser);
        when(jwtUtils.generateToken(testUser, 1L)).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(any(HashMap.class), eq(testUser))).thenReturn("refresh-token");

        SignInResponse response = authService.signIn(request);

        assertEquals("access-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(Duration.ofHours(24), response.getExpirationTime());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void signIn_wrongUserType_shouldThrowIllegalStateException() {
        SignInRequest request = new SignInRequest("password123", "john.doe");

        UserDetails mockDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("john.doe")).thenReturn(mockDetails);

        assertThrows(IllegalStateException.class, () -> authService.signIn(request));
    }

    @Test
    void signIn_authenticationFails_shouldThrowAuthenticationException() {
        SignInRequest request = new SignInRequest("password123", "john.doe");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authService.signIn(request));
    }

    @Test
    void refreshToken_success() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setUsername("john.doe");
        request.setPassword("password123");
        request.setRefreshToken("valid-refresh-token");

        when(jwtUtils.extractUsername("valid-refresh-token")).thenReturn("john.doe");
        when(userDetailsService.loadUserByUsername("john.doe")).thenReturn(testUser);
        when(jwtUtils.isTokenValid("valid-refresh-token", testUser)).thenReturn(true);
        when(jwtUtils.generateToken(testUser, 1L)).thenReturn("new-access-token");

        SignInResponse response = authService.refreshToken(request);

        assertEquals("new-access-token", response.getToken());
        assertEquals("valid-refresh-token", response.getRefreshToken());
        assertEquals(Duration.ofHours(24), response.getExpirationTime());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void refreshToken_tokenInvalid_shouldThrowBadCredentialsException() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setUsername("john.doe");
        request.setPassword("password123");
        request.setRefreshToken("invalid-refresh-token");

        when(jwtUtils.extractUsername("invalid-refresh-token")).thenReturn("john.doe");
        when(userDetailsService.loadUserByUsername("john.doe")).thenReturn(testUser);
        when(jwtUtils.isTokenValid("invalid-refresh-token", testUser)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(request));
    }

    @Test
    void refreshToken_wrongUserType_shouldThrowIllegalStateException() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setUsername("john.doe");
        request.setPassword("password123");
        request.setRefreshToken("valid-refresh-token");

        when(jwtUtils.extractUsername("valid-refresh-token")).thenReturn("john.doe");

        UserDetails mockUser = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("john.doe")).thenReturn(mockUser);
        when(jwtUtils.isTokenValid(eq("valid-refresh-token"), any(UserDetails.class))).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> authService.refreshToken(request));
    }

    @Test
    void refreshToken_authenticationFails_shouldThrowAuthenticationException() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setUsername("john.doe");
        request.setPassword("wrong-password");
        request.setRefreshToken("valid-refresh-token");

        when(jwtUtils.extractUsername("valid-refresh-token")).thenReturn("john.doe");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(request));
    }
}
