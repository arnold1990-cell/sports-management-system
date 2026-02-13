package com.sportsms.auth;

import com.sportsms.common.NotFoundException;
import com.sportsms.user.Role;
import com.sportsms.user.User;
import com.sportsms.user.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final long refreshExpirationMinutes;
    private final Environment environment;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       Environment environment,
                       @Value("${app.jwt.refresh-expiration-minutes}") long refreshExpirationMinutes) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.environment = environment;
        this.refreshExpirationMinutes = refreshExpirationMinutes;
    }

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.VIEWER));
        userRepository.save(user);
        return issueTokens(user);
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        if (isDevProfileActive()) {
            userRepository.findByEmail(request.email()).ifPresentOrElse(user -> {
                boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPassword());
                log.debug("Login diagnostic for {}: userFound=true, encoder={}, passwordMatches={}, accountNonLocked=true, accountEnabled=true, accountNonExpired=true, credentialsNonExpired=true",
                        request.email(), passwordEncoder.getClass().getSimpleName(), passwordMatches);
            }, () -> log.debug("Login diagnostic for {}: userFound=false, encoder={}",
                    request.email(), passwordEncoder.getClass().getSimpleName()));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return issueTokens(user);
    }

    private boolean isDevProfileActive() {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch("dev"::equalsIgnoreCase);
    }

    public AuthDto.AuthResponse refresh(AuthDto.RefreshRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));
        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }
        return issueTokens(token.getUser());
    }

    public void logout(AuthDto.RefreshRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public AuthProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Set<String> roles = user.getRoles().stream().map(Role::name).collect(Collectors.toSet());
        return new AuthProfileResponse(user.getId(), user.getEmail(), user.getFullName(), roles);
    }

    private AuthDto.AuthResponse issueTokens(User user) {
        Set<String> roles = user.getRoles().stream().map(Role::name).collect(Collectors.toSet());
        String accessToken = jwtService.generateToken(user.getEmail(), roles.stream().toList());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plus(refreshExpirationMinutes, ChronoUnit.MINUTES));
        refreshTokenRepository.save(refreshToken);
        return new AuthDto.AuthResponse(accessToken, refreshToken.getToken(), roles);
    }
}
