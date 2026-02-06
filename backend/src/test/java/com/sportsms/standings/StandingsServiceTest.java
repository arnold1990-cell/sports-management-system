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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandingsServiceTest {
    @Mock
    private FixtureRepository fixtureRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @InjectMocks
    private StandingsService standingsService;

    @Test
    void getStandingsCalculatesPoints() {
        UUID competitionId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();

        Team home = new Team();
        home.setId(UUID.randomUUID());
        home.setName("Home FC");

        Team away = new Team();
        away.setId(UUID.randomUUID());
        away.setName("Away FC");

        Competition competition = new Competition();
        competition.setId(competitionId);
        competition.setTeams(Set.of(home, away));

        Season season = new Season();
        season.setId(seasonId);

        Fixture finishedFixture = new Fixture();
        finishedFixture.setHomeTeam(home);
        finishedFixture.setAwayTeam(away);
        finishedFixture.setStatus(MatchStatus.FINISHED);
        finishedFixture.setHomeScore(2);
        finishedFixture.setAwayScore(1);

        Fixture pendingFixture = new Fixture();
        pendingFixture.setHomeTeam(home);
        pendingFixture.setAwayTeam(away);
        pendingFixture.setStatus(MatchStatus.SCHEDULED);

        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(fixtureRepository.filterFixtures(competitionId, seasonId, null, null, null))
                .thenReturn(List.of(finishedFixture, pendingFixture));

        StandingsDto.StandingsResponse response = standingsService.getStandings(competitionId, seasonId);

        StandingsDto.TeamStanding homeRow = response.table().stream()
                .filter(row -> row.teamId().equals(home.getId()))
                .findFirst()
                .orElseThrow();
        StandingsDto.TeamStanding awayRow = response.table().stream()
                .filter(row -> row.teamId().equals(away.getId()))
                .findFirst()
                .orElseThrow();

        Assertions.assertEquals(3, homeRow.points());
        Assertions.assertEquals(0, awayRow.points());
        Assertions.assertEquals(2, homeRow.goalsFor());
        Assertions.assertEquals(1, awayRow.goalsFor());
    }

    @Test
    void getStandingsThrowsWhenCompetitionMissing() {
        UUID competitionId = UUID.randomUUID();
        when(competitionRepository.findById(competitionId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,
                () -> standingsService.getStandings(competitionId, UUID.randomUUID()));
    }
}
