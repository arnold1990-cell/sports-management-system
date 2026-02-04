package com.sportsms.fixture;

import com.sportsms.competition.Competition;
import com.sportsms.competition.CompetitionRepository;
import com.sportsms.competition.Season;
import com.sportsms.competition.SeasonRepository;
import com.sportsms.common.NotFoundException;
import com.sportsms.team.Team;
import com.sportsms.team.TeamRepository;
import com.sportsms.user.User;
import com.sportsms.user.UserRepository;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class FixtureService {
    private final FixtureRepository fixtureRepository;
    private final FixtureGoalRepository fixtureGoalRepository;
    private final TeamRepository teamRepository;
    private final CompetitionRepository competitionRepository;
    private final SeasonRepository seasonRepository;
    private final UserRepository userRepository;

    public FixtureService(FixtureRepository fixtureRepository,
                          FixtureGoalRepository fixtureGoalRepository,
                          TeamRepository teamRepository,
                          CompetitionRepository competitionRepository,
                          SeasonRepository seasonRepository,
                          UserRepository userRepository) {
        this.fixtureRepository = fixtureRepository;
        this.fixtureGoalRepository = fixtureGoalRepository;
        this.teamRepository = teamRepository;
        this.competitionRepository = competitionRepository;
        this.seasonRepository = seasonRepository;
        this.userRepository = userRepository;
    }

    public List<Fixture> list(UUID competitionId, UUID seasonId, UUID teamId, OffsetDateTime from, OffsetDateTime to) {
        return fixtureRepository.filterFixtures(competitionId, seasonId, teamId, from, to);
    }

    public Fixture create(FixtureDto.FixtureCreateRequest request) {
        Fixture fixture = new Fixture();
        applyCreateRequest(fixture, request);
        return fixtureRepository.save(fixture);
    }

    public Fixture update(UUID id, FixtureDto.FixtureRequest request) {
        Fixture fixture = fixtureRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixture not found"));
        applyRequest(fixture, request);
        return fixtureRepository.save(fixture);
    }

    private void applyCreateRequest(Fixture fixture, FixtureDto.FixtureCreateRequest request) {
        validateTeamPair(request.homeTeamId(), request.awayTeamId());
        Team homeTeam = teamRepository.findById(request.homeTeamId())
                .orElseThrow(() -> new NotFoundException("Home team not found"));
        Team awayTeam = teamRepository.findById(request.awayTeamId())
                .orElseThrow(() -> new NotFoundException("Away team not found"));
        Competition competition = competitionRepository.findById(request.competitionId())
                .orElseThrow(() -> new NotFoundException("Competition not found"));
        Season season = seasonRepository.findById(request.seasonId())
                .orElseThrow(() -> new NotFoundException("Season not found"));
        fixture.setHomeTeam(homeTeam);
        fixture.setAwayTeam(awayTeam);
        fixture.setCompetition(competition);
        fixture.setSeason(season);
        setReferee(fixture, request.refereeId());
        fixture.setVenue(request.venue());
        fixture.setMatchDate(request.kickoffTime().atZone(ZoneId.systemDefault()).toOffsetDateTime());
        fixture.setStatus(request.status() != null ? request.status() : MatchStatus.SCHEDULED);
        fixture.setHomeScore(request.homeScore());
        fixture.setAwayScore(request.awayScore());
    }

    private void applyRequest(Fixture fixture, FixtureDto.FixtureRequest request) {
        validateTeamPair(request.homeTeamId(), request.awayTeamId());
        Team homeTeam = teamRepository.findById(request.homeTeamId())
                .orElseThrow(() -> new NotFoundException("Home team not found"));
        Team awayTeam = teamRepository.findById(request.awayTeamId())
                .orElseThrow(() -> new NotFoundException("Away team not found"));
        Competition competition = competitionRepository.findById(request.competitionId())
                .orElseThrow(() -> new NotFoundException("Competition not found"));
        Season season = seasonRepository.findById(request.seasonId())
                .orElseThrow(() -> new NotFoundException("Season not found"));
        fixture.setHomeTeam(homeTeam);
        fixture.setAwayTeam(awayTeam);
        fixture.setCompetition(competition);
        fixture.setSeason(season);
        setReferee(fixture, request.refereeId());
        fixture.setVenue(request.venue());
        fixture.setMatchDate(request.matchDate());
        fixture.setStatus(request.status());
        fixture.setHomeScore(request.homeScore());
        fixture.setAwayScore(request.awayScore());
        if (request.goals() != null) {
            fixture.getGoals().clear();
            request.goals().forEach(goalRequest -> {
                FixtureGoal goal = new FixtureGoal();
                goal.setFixture(fixture);
                Team team = teamRepository.findById(goalRequest.teamId())
                        .orElseThrow(() -> new NotFoundException("Team not found"));
                goal.setTeam(team);
                goal.setScorerName(goalRequest.scorerName());
                goal.setMinute(goalRequest.minute());
                fixture.getGoals().add(goal);
            });
        }
    }

    private void validateTeamPair(UUID homeTeamId, UUID awayTeamId) {
        if (homeTeamId != null && homeTeamId.equals(awayTeamId)) {
            throw new IllegalArgumentException("Home team and away team must be different");
        }
    }

    private void setReferee(Fixture fixture, UUID refereeId) {
        if (refereeId != null) {
            User referee = userRepository.findById(refereeId)
                    .orElseThrow(() -> new NotFoundException("Referee not found"));
            fixture.setReferee(referee);
        } else {
            fixture.setReferee(null);
        }
    }
}
