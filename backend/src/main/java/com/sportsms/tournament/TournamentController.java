package com.sportsms.tournament;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {
    @PostMapping("/round-robin/generate") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<Map<String, Object>> roundRobin(@RequestBody Map<String, List<String>> request) {
        List<String> teams = request.getOrDefault("teams", List.of());
        return teams.stream().map(t -> Map.of("team", t, "seed", teams.indexOf(t) + 1)).toList();
    }

    @GetMapping("/rankings") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')")
    public List<Map<String, Object>> rankings() { return List.of(Map.of("team", "Sample", "points", 0, "fairPlay", 0)); }
}
