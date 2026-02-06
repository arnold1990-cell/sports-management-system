package com.sportsms.team;

import com.sportsms.club.Club;
import com.sportsms.club.ClubRepository;
import com.sportsms.common.NotFoundException;
import java.util.Optional;
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
class TeamServiceTest {
    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private TeamService teamService;

    @Test
    void createSetsClubWhenProvided() {
        UUID clubId = UUID.randomUUID();
        Club club = new Club();
        club.setId(clubId);
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamDto.TeamRequest request = new TeamDto.TeamRequest("Falcons", clubId, "Coach A", "Ground", "logo.png");
        Team result = teamService.create(request);

        Assertions.assertEquals(club, result.getClub());
        Assertions.assertEquals("Falcons", result.getName());
    }

    @Test
    void createClearsClubWhenMissing() {
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamDto.TeamRequest request = new TeamDto.TeamRequest("Falcons", null, "Coach A", "Ground", "logo.png");
        Team result = teamService.create(request);

        Assertions.assertNull(result.getClub());
    }

    @Test
    void updateThrowsWhenMissing() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,
                () -> teamService.update(teamId, new TeamDto.TeamRequest("Name", null, "Coach", "Ground", null)));
    }

    @Test
    void deleteThrowsWhenMissing() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.existsById(teamId)).thenReturn(false);

        Assertions.assertThrows(NotFoundException.class, () -> teamService.delete(teamId));
    }
}
