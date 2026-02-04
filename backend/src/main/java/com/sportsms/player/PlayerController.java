package com.sportsms.player;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public List<PlayerDto.PlayerResponse> list(@RequestParam(name = "teamId", required = false) UUID teamId) {
        return playerService.findAll(teamId).stream()
                .map(player -> new PlayerDto.PlayerResponse(
                        player.getId(),
                        player.getTeam() != null ? player.getTeam().getId() : null,
                        player.getTeam() != null ? player.getTeam().getName() : null,
                        player.getFirstName(),
                        player.getLastName(),
                        player.getDob(),
                        player.getPosition(),
                        player.getJerseyNumber(),
                        player.getStatus(),
                        player.getStatsSummary(),
                        player.getCreatedAt()))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')")
    public PlayerDto.PlayerResponse create(@Valid @RequestBody PlayerDto.PlayerRequest request) {
        Player player = playerService.create(request);
        return new PlayerDto.PlayerResponse(player.getId(),
                player.getTeam() != null ? player.getTeam().getId() : null,
                player.getTeam() != null ? player.getTeam().getName() : null,
                player.getFirstName(),
                player.getLastName(),
                player.getDob(),
                player.getPosition(),
                player.getJerseyNumber(),
                player.getStatus(),
                player.getStatsSummary(),
                player.getCreatedAt());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')")
    public PlayerDto.PlayerResponse update(@PathVariable("id") UUID id, @Valid @RequestBody PlayerDto.PlayerRequest request) {
        Player player = playerService.update(id, request);
        return new PlayerDto.PlayerResponse(player.getId(),
                player.getTeam() != null ? player.getTeam().getId() : null,
                player.getTeam() != null ? player.getTeam().getName() : null,
                player.getFirstName(),
                player.getLastName(),
                player.getDob(),
                player.getPosition(),
                player.getJerseyNumber(),
                player.getStatus(),
                player.getStatsSummary(),
                player.getCreatedAt());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable("id") UUID id) {
        playerService.delete(id);
    }
}
