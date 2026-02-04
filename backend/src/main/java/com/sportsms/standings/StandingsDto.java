package com.sportsms.standings;

import java.util.List;
import java.util.UUID;

public class StandingsDto {
    public record TeamStanding(UUID teamId, String teamName, int played, int won, int drawn, int lost,
                               int goalsFor, int goalsAgainst, int goalDifference, int points) {}

    public record StandingsResponse(UUID competitionId, UUID seasonId, List<TeamStanding> table) {}
}
