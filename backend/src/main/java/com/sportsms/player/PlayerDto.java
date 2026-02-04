package com.sportsms.player;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class PlayerDto {
    public record PlayerResponse(UUID id, UUID teamId, String teamName, String firstName, String lastName,
                                 LocalDate dob, String position, Integer jerseyNumber, PlayerStatus status,
                                 String statsSummary, Instant createdAt) {}

    public record PlayerRequest(UUID teamId,
                                @NotBlank String firstName,
                                @NotBlank String lastName,
                                @NotNull LocalDate dob,
                                @NotBlank String position,
                                Integer jerseyNumber,
                                @NotNull PlayerStatus status,
                                String statsSummary) {}
}
