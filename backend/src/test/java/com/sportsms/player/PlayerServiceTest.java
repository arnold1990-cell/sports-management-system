package com.sportsms.player;

import com.sportsms.common.NotFoundException;
import com.sportsms.team.Team;
import com.sportsms.team.TeamRepository;
import java.time.LocalDate;
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
class PlayerServiceTest {
    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void findAllUsesTeamFilterWhenProvided() {
        UUID teamId = UUID.randomUUID();
        when(playerRepository.findByTeamId(teamId)).thenReturn(List.of());

        playerService.findAll(teamId);

        verify(playerRepository).findByTeamId(teamId);
    }

    @Test
    void createAssignsTeamWhenProvided() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlayerDto.PlayerRequest request = new PlayerDto.PlayerRequest(teamId, "Ada", "Lovelace",
                LocalDate.of(2000, 1, 1), "MID", 10, PlayerStatus.ACTIVE, "Stats");
        Player result = playerService.create(request);

        Assertions.assertEquals(team, result.getTeam());
        Assertions.assertEquals("Ada", result.getFirstName());
    }

    @Test
    void updateThrowsWhenMissing() {
        UUID playerId = UUID.randomUUID();
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        PlayerDto.PlayerRequest request = new PlayerDto.PlayerRequest(null, "Ada", "Lovelace",
                LocalDate.of(2000, 1, 1), "MID", 10, PlayerStatus.ACTIVE, "Stats");

        Assertions.assertThrows(NotFoundException.class, () -> playerService.update(playerId, request));
    }

    @Test
    void deleteThrowsWhenMissing() {
        UUID playerId = UUID.randomUUID();
        when(playerRepository.existsById(playerId)).thenReturn(false);

        Assertions.assertThrows(NotFoundException.class, () -> playerService.delete(playerId));
    }
}
