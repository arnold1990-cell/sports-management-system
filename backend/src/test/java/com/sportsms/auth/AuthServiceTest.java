package com.sportsms.auth;

import com.sportsms.common.NotFoundException;
import com.sportsms.user.Role;
import com.sportsms.user.User;
import com.sportsms.user.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Environment environment;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        authService = new AuthService(userRepository, refreshTokenRepository, passwordEncoder, jwtService, authenticationManager, environment, 60L);
    }

    @Test
    void registerAssignsViewerRole() {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest("user@example.com", "password123", "User Name");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded");
        when(jwtService.generateToken(any(String.class), any())).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthDto.AuthResponse response = authService.register(request);

        Assertions.assertEquals("access-token", response.accessToken());
        Assertions.assertTrue(response.roles().contains(Role.VIEWER.name()));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest("user@example.com", "password123", "User Name");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        Assertions.assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }


    @Test
    void loginSucceedsForValidCredentials() {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest("user@example.com", "password123");
        User user = new User();
        user.setEmail(request.email());
        user.setRoles(Set.of(Role.ADMIN));

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(String.class), any())).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthDto.AuthResponse response = authService.login(request);

        Assertions.assertEquals("access-token", response.accessToken());
        Assertions.assertTrue(response.roles().contains(Role.ADMIN.name()));
    }

    @Test
    void loginFailsWhenAuthenticationIsNotAuthenticated() {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest("user@example.com", "wrong-password");
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        Assertions.assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> authService.login(request));
    }

    @Test
    void refreshThrowsWhenTokenMissing() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,
                () -> authService.refresh(new AuthDto.RefreshRequest("missing")));
    }

    @Test
    void refreshThrowsWhenTokenRevoked() {
        RefreshToken token = new RefreshToken();
        token.setToken("revoked");
        token.setRevoked(true);
        token.setExpiresAt(Instant.now().plusSeconds(600));
        when(refreshTokenRepository.findByToken("revoked")).thenReturn(Optional.of(token));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> authService.refresh(new AuthDto.RefreshRequest("revoked")));
    }
}
