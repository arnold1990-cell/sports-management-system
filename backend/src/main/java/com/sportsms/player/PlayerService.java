package com.sportsms.player;

import com.sportsms.common.NotFoundException;
import com.sportsms.team.Team;
import com.sportsms.team.TeamRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    public PlayerService(PlayerRepository playerRepository, TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    public List<Player> findAll(UUID teamId) {
        if (teamId != null) {
            return playerRepository.findByTeamId(teamId);
        }
        return playerRepository.findAll();
    }

    public Player create(PlayerDto.PlayerRequest request) {
        Player player = new Player();
        applyRequest(player, request);
        return playerRepository.save(player);
    }

    public Player update(UUID id, PlayerDto.PlayerRequest request) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        applyRequest(player, request);
        return playerRepository.save(player);
    }

    public void delete(UUID id) {
        if (!playerRepository.existsById(id)) {
            throw new NotFoundException("Player not found");
        }
        playerRepository.deleteById(id);
    }

    private void applyRequest(Player player, PlayerDto.PlayerRequest request) {
        if (request.teamId() != null) {
            Team team = teamRepository.findById(request.teamId())
                    .orElseThrow(() -> new NotFoundException("Team not found"));
            player.setTeam(team);
        } else {
            player.setTeam(null);
        }
        player.setFirstName(request.firstName());
        player.setLastName(request.lastName());
        player.setDob(request.dob());
        player.setPosition(request.position());
        player.setJerseyNumber(request.jerseyNumber());
        player.setStatus(request.status());
        player.setStatsSummary(request.statsSummary());
    }
}
