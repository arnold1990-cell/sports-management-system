package com.sportsms.fixture;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class FixtureDto {
    public record GoalEventRequest(@NotBlank String scorerName, @NotNull Integer minute, @NotNull UUID teamId) {}

    public record FixtureCreateRequest(@NotNull UUID homeTeamId,
                                       @NotNull UUID awayTeamId,
                                       @NotNull UUID competitionId,
                                       @NotNull UUID seasonId,
                                       UUID refereeId,
                                       @NotBlank String venue,
                                       @JsonAlias("matchDate") @NotNull LocalDateTime kickoffTime,
                                       MatchStatus status,
                                       Integer homeScore,
                                       Integer awayScore) {}

    public record FixtureRequest(@NotNull UUID homeTeamId,
                                 @NotNull UUID awayTeamId,
                                 @NotNull UUID competitionId,
                                 @NotNull UUID seasonId,
                                 UUID refereeId,
                                 @NotBlank String venue,
                                 @NotNull OffsetDateTime matchDate,
                                 @NotNull MatchStatus status,
                                 Integer homeScore,
                                 Integer awayScore,
                                 List<GoalEventRequest> goals) {}

    public record FixtureResponse(UUID id,
                                  UUID homeTeamId,
                                  String homeTeamName,
                                  UUID awayTeamId,
                                  String awayTeamName,
                                  UUID competitionId,
                                  String competitionName,
                                  UUID seasonId,
                                  String seasonName,
                                  UUID refereeId,
                                  String refereeName,
                                  String venue,
                                  OffsetDateTime matchDate,
                                  MatchStatus status,
                                  Integer homeScore,
                                  Integer awayScore,
                                  List<GoalEventResponse> goals) {}

    public record GoalEventResponse(UUID id, UUID teamId, String teamName, String scorerName, Integer minute) {}
}
