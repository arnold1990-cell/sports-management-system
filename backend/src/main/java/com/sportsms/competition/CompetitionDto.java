package com.sportsms.competition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public class CompetitionDto {
    public record SeasonResponse(UUID id, String name, LocalDate startDate, LocalDate endDate) {}

    public record SeasonRequest(@NotBlank String name, @NotNull LocalDate startDate, @NotNull LocalDate endDate) {}

    public record CompetitionResponse(UUID id, String name, CompetitionType type, Set<UUID> teamIds) {}

    public record CompetitionRequest(@NotBlank String name, @NotNull CompetitionType type, Set<UUID> teamIds) {}
}
