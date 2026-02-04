package com.sportsms.club;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public class ClubDto {
    public record ClubResponse(UUID id, String name, String city, Instant createdAt) {}

    public record ClubRequest(@NotBlank String name, String city) {}
}
