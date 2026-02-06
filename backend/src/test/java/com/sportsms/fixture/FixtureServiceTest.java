package com.sportsms.fixture;

import com.sportsms.common.NotFoundException;
import com.sportsms.competition.Competition;
import com.sportsms.competition.CompetitionRepository;
import com.sportsms.competition.Season;
import com.sportsms.competition.SeasonRepository;
import com.sportsms.team.Team;
import com.sportsms.team.TeamRepository;
import com.sportsms.user.User;
import com.sportsms.user.UserRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FixtureServiceTest {
    @Mock
    private FixtureRepository fixtureRepository;

    @Mock
    private FixtureGoalRepository fixtureGoalRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FixtureService fixtureService;

    @Test
    void listDelegatesToRepository() {
        UUID competitionId = UUID.randomUUID();
        List<Fixture> fixtures = List.of(new Fixture());
        when(fixtureRepository.filterFixtures(competitionId, null, null, null, null)).thenReturn(fixtures);

        List<Fixture> result = fixtureService.list(competitionId, null, null, null, null);

        Assertions.assertEquals(fixtures, result);
    }

    @Test
    void createSetsDefaultsAndAssociations() {
        UUID homeId = UUID.randomUUID();
        UUID awayId = UUID.randomUUID();
        UUID competitionId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID refereeId = UUID.randomUUID();

        Team home = new Team();
        home.setId(homeId);
        Team away = new Team();
        away.setId(awayId);
        Competition competition = new Competition();
        competition.setId(competitionId);
        Season season = new Season();
        season.setId(seasonId);
        User referee = new User();
        referee.setId(refereeId);

        when(teamRepository.findById(homeId)).thenReturn(Optional.of(home));
        when(teamRepository.findById(awayId)).thenReturn(Optional.of(away));
        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(userRepository.findById(refereeId)).thenReturn(Optional.of(referee));
        when(fixtureRepository.save(any(Fixture.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime kickoff = LocalDateTime.of(2024, 1, 10, 18, 0);
        FixtureDto.FixtureCreateRequest request = new FixtureDto.FixtureCreateRequest(
                homeId, awayId, competitionId, seasonId, refereeId, "National Stadium", kickoff,
                null, 2, 1);

        Fixture result = fixtureService.create(request);

        Assertions.assertEquals(home, result.getHomeTeam());
        Assertions.assertEquals(away, result.getAwayTeam());
        Assertions.assertEquals(competition, result.getCompetition());
        Assertions.assertEquals(season, result.getSeason());
        Assertions.assertEquals(referee, result.getReferee());
        Assertions.assertEquals("National Stadium", result.getVenue());
        Assertions.assertEquals(MatchStatus.SCHEDULED, result.getStatus());
        Assertions.assertEquals(kickoff.atZone(ZoneId.systemDefault()).toOffsetDateTime(), result.getMatchDate());
    }

    @Test
    void createThrowsWhenTeamsMatch() {
        UUID teamId = UUID.randomUUID();
        FixtureDto.FixtureCreateRequest request = new FixtureDto.FixtureCreateRequest(
                teamId, teamId, UUID.randomUUID(), UUID.randomUUID(), null, "Venue",
                LocalDateTime.now(), MatchStatus.SCHEDULED, null, null);

        Assertions.assertThrows(IllegalArgumentException.class, () -> fixtureService.create(request));
    }

    @Test
    void updateReplacesGoals() {
        UUID fixtureId = UUID.randomUUID();
        UUID homeId = UUID.randomUUID();
        UUID awayId = UUID.randomUUID();
        UUID competitionId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID goalTeamId = UUID.randomUUID();

        Fixture fixture = new Fixture();
        fixture.setId(fixtureId);
        fixture.getGoals().add(new FixtureGoal());

        Team home = new Team();
        home.setId(homeId);
        Team away = new Team();
        away.setId(awayId);
        Competition competition = new Competition();
        competition.setId(competitionId);
        Season season = new Season();
        season.setId(seasonId);
        Team goalTeam = new Team();
        goalTeam.setId(goalTeamId);

        when(fixtureRepository.findById(fixtureId)).thenReturn(Optional.of(fixture));
        when(teamRepository.findById(homeId)).thenReturn(Optional.of(home));
        when(teamRepository.findById(awayId)).thenReturn(Optional.of(away));
        when(teamRepository.findById(goalTeamId)).thenReturn(Optional.of(goalTeam));
        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(fixtureRepository.save(any(Fixture.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FixtureDto.GoalEventRequest goalRequest = new FixtureDto.GoalEventRequest("Player", 12, goalTeamId);
        FixtureDto.FixtureRequest request = new FixtureDto.FixtureRequest(
                homeId, awayId, competitionId, seasonId, null, "Venue",
                OffsetDateTime.now(), MatchStatus.FINISHED, 1, 0, List.of(goalRequest));

        Fixture result = fixtureService.update(fixtureId, request);

        Assertions.assertEquals(1, result.getGoals().size());
        Assertions.assertEquals(goalTeam, result.getGoals().get(0).getTeam());
    }

    @Test
    void updateThrowsWhenGoalTeamMissing() {
        UUID fixtureId = UUID.randomUUID();
        UUID homeId = UUID.randomUUID();
        UUID awayId = UUID.randomUUID();
        UUID competitionId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID goalTeamId = UUID.randomUUID();

        Fixture fixture = new Fixture();
        fixture.setId(fixtureId);

        Team home = new Team();
        home.setId(homeId);
        Team away = new Team();
        away.setId(awayId);
        Competition competition = new Competition();
        competition.setId(competitionId);
        Season season = new Season();
        season.setId(seasonId);

        when(fixtureRepository.findById(fixtureId)).thenReturn(Optional.of(fixture));
        when(teamRepository.findById(homeId)).thenReturn(Optional.of(home));
        when(teamRepository.findById(awayId)).thenReturn(Optional.of(away));
        when(teamRepository.findById(goalTeamId)).thenReturn(Optional.empty());
        when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));

        FixtureDto.GoalEventRequest goalRequest = new FixtureDto.GoalEventRequest("Player", 12, goalTeamId);
        FixtureDto.FixtureRequest request = new FixtureDto.FixtureRequest(
                homeId, awayId, competitionId, seasonId, null, "Venue",
                OffsetDateTime.now(), MatchStatus.FINISHED, 1, 0, List.of(goalRequest));

        Assertions.assertThrows(NotFoundException.class, () -> fixtureService.update(fixtureId, request));
    }
}
