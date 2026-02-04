package com.sportsms.common;

import com.sportsms.fixture.MatchStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class DashboardDto {
    public record Summary(long clubs, long teams, long players, long fixtures, long posts) {}

    public record FixtureCard(UUID id, String homeTeam, String awayTeam, OffsetDateTime matchDate,
                              MatchStatus status, Integer homeScore, Integer awayScore) {}

    public record DashboardResponse(Summary summary, List<FixtureCard> latestFixtures, List<String> latestPosts) {}
}
