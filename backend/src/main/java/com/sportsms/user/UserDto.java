package com.sportsms.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class UserDto {
    public record UserResponse(UUID id, String email, String fullName, Set<Role> roles, Instant createdAt) {}

    public record CreateUserRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank String fullName,
            @NotEmpty Set<Role> roles) {}

    public record UpdateRolesRequest(@NotEmpty Set<Role> roles) {}
}
