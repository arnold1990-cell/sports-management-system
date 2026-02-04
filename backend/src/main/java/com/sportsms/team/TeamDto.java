package com.sportsms.team;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public class TeamDto {
    public record TeamResponse(UUID id, String name, UUID clubId, String clubName, String coachName,
                               String homeGround, String logoUrl, Instant createdAt) {}

    public record TeamRequest(@NotBlank String name, UUID clubId, String coachName, String homeGround, String logoUrl) {}
}
