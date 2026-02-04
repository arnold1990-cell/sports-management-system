package com.sportsms.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public class AuthDto {
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record RegisterRequest(@Email @NotBlank String email,
                                  @NotBlank @Size(min = 8) String password,
                                  @NotBlank String fullName) {}

    public record AuthResponse(String accessToken, String refreshToken, Set<String> roles) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}
}
