package com.sportsms.standings;

import com.sportsms.common.NotFoundException;
import com.sportsms.competition.Competition;
import com.sportsms.competition.CompetitionRepository;
import com.sportsms.competition.Season;
import com.sportsms.competition.SeasonRepository;
import com.sportsms.fixture.Fixture;
import com.sportsms.fixture.FixtureRepository;
import com.sportsms.fixture.MatchStatus;
import com.sportsms.team.Team;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class StandingsService {
    private final FixtureRepository fixtureRepository;
    private final CompetitionRepository competitionRepository;
    private final SeasonRepository seasonRepository;

    public StandingsService(FixtureRepository fixtureRepository,
                            CompetitionRepository competitionRepository,
                            SeasonRepository seasonRepository) {
        this.fixtureRepository = fixtureRepository;
        this.competitionRepository = competitionRepository;
        this.seasonRepository = seasonRepository;
    }

    public StandingsDto.StandingsResponse getStandings(UUID competitionId, UUID seasonId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new NotFoundException("Competition not found"));
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new NotFoundException("Season not found"));
        List<Fixture> fixtures = fixtureRepository.filterFixtures(competitionId, seasonId, null, null, null);
        Map<UUID, StandingsRow> table = new HashMap<>();
        for (Team team : competition.getTeams()) {
            table.put(team.getId(), new StandingsRow(team.getId(), team.getName()));
        }
        for (Fixture fixture : fixtures) {
            if (fixture.getStatus() != MatchStatus.FINISHED || fixture.getHomeScore() == null || fixture.getAwayScore() == null) {
                continue;
            }
            StandingsRow home = table.computeIfAbsent(fixture.getHomeTeam().getId(),
                    id -> new StandingsRow(id, fixture.getHomeTeam().getName()));
            StandingsRow away = table.computeIfAbsent(fixture.getAwayTeam().getId(),
                    id -> new StandingsRow(id, fixture.getAwayTeam().getName()));
            home.played++;
            away.played++;
            home.goalsFor += fixture.getHomeScore();
            home.goalsAgainst += fixture.getAwayScore();
            away.goalsFor += fixture.getAwayScore();
            away.goalsAgainst += fixture.getHomeScore();
            if (fixture.getHomeScore() > fixture.getAwayScore()) {
                home.won++;
                away.lost++;
                home.points += 3;
            } else if (fixture.getHomeScore() < fixture.getAwayScore()) {
                away.won++;
                home.lost++;
                away.points += 3;
            } else {
                home.drawn++;
                away.drawn++;
                home.points += 1;
                away.points += 1;
            }
        }
        List<StandingsDto.TeamStanding> standings = new ArrayList<>();
        for (StandingsRow row : table.values()) {
            row.goalDifference = row.goalsFor - row.goalsAgainst;
            standings.add(row.toDto());
        }
        standings.sort(Comparator.comparingInt(StandingsDto.TeamStanding::points).reversed()
                .thenComparingInt(StandingsDto.TeamStanding::goalDifference).reversed()
                .thenComparingInt(StandingsDto.TeamStanding::goalsFor).reversed());
        return new StandingsDto.StandingsResponse(competition.getId(), season.getId(), standings);
    }

    private static class StandingsRow {
        private final UUID teamId;
        private final String teamName;
        private int played;
        private int won;
        private int drawn;
        private int lost;
        private int goalsFor;
        private int goalsAgainst;
        private int goalDifference;
        private int points;

        private StandingsRow(UUID teamId, String teamName) {
            this.teamId = teamId;
            this.teamName = teamName;
        }

        private StandingsDto.TeamStanding toDto() {
            return new StandingsDto.TeamStanding(teamId, teamName, played, won, drawn, lost,
                    goalsFor, goalsAgainst, goalDifference, points);
        }
    }
}
