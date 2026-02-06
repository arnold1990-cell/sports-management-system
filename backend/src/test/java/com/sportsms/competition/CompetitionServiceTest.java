package com.sportsms.competition;

import com.sportsms.common.NotFoundException;
import com.sportsms.team.Team;
import com.sportsms.team.TeamRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionServiceTest {
    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private CompetitionService competitionService;

    @Test
    void createSeasonPersistsDates() {
        CompetitionDto.SeasonRequest request = new CompetitionDto.SeasonRequest("2024/25",
                LocalDate.of(2024, 8, 1), LocalDate.of(2025, 5, 1));
        when(seasonRepository.save(any(Season.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Season result = competitionService.createSeason(request);

        Assertions.assertEquals("2024/25", result.getName());
        Assertions.assertEquals(LocalDate.of(2024, 8, 1), result.getStartDate());
    }

    @Test
    void updateSeasonThrowsWhenMissing() {
        UUID seasonId = UUID.randomUUID();
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.empty());

        CompetitionDto.SeasonRequest request = new CompetitionDto.SeasonRequest("2024/25",
                LocalDate.of(2024, 8, 1), LocalDate.of(2025, 5, 1));

        Assertions.assertThrows(NotFoundException.class, () -> competitionService.updateSeason(seasonId, request));
    }

    @Test
    void deleteSeasonThrowsWhenMissing() {
        UUID seasonId = UUID.randomUUID();
        when(seasonRepository.existsById(seasonId)).thenReturn(false);

        Assertions.assertThrows(NotFoundException.class, () -> competitionService.deleteSeason(seasonId));
    }

    @Test
    void createCompetitionAssignsTeams() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(competitionRepository.save(any(Competition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompetitionDto.CompetitionRequest request = new CompetitionDto.CompetitionRequest("Premier League",
                CompetitionType.LEAGUE, Set.of(teamId));
        Competition result = competitionService.createCompetition(request);

        Assertions.assertTrue(result.getTeams().contains(team));
        Assertions.assertEquals("Premier League", result.getName());
    }

    @Test
    void createCompetitionThrowsWhenTeamMissing() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        CompetitionDto.CompetitionRequest request = new CompetitionDto.CompetitionRequest("Premier League",
                CompetitionType.LEAGUE, Set.of(teamId));

        Assertions.assertThrows(NotFoundException.class, () -> competitionService.createCompetition(request));
    }

    @Test
    void updateCompetitionThrowsWhenMissing() {
        UUID competitionId = UUID.randomUUID();
        when(competitionRepository.findById(competitionId)).thenReturn(Optional.empty());

        CompetitionDto.CompetitionRequest request = new CompetitionDto.CompetitionRequest("Premier League",
                CompetitionType.LEAGUE, Set.of());

        Assertions.assertThrows(NotFoundException.class, () -> competitionService.updateCompetition(competitionId, request));
    }

    @Test
    void deleteCompetitionThrowsWhenMissing() {
        UUID competitionId = UUID.randomUUID();
        when(competitionRepository.existsById(competitionId)).thenReturn(false);

        Assertions.assertThrows(NotFoundException.class, () -> competitionService.deleteCompetition(competitionId));
    }
}
