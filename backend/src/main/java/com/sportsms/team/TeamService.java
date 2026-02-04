package com.sportsms.team;

import com.sportsms.club.Club;
import com.sportsms.club.ClubRepository;
import com.sportsms.common.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final ClubRepository clubRepository;

    public TeamService(TeamRepository teamRepository, ClubRepository clubRepository) {
        this.teamRepository = teamRepository;
        this.clubRepository = clubRepository;
    }

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    public Team create(TeamDto.TeamRequest request) {
        Team team = new Team();
        applyRequest(team, request);
        return teamRepository.save(team);
    }

    public Team update(UUID id, TeamDto.TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Team not found"));
        applyRequest(team, request);
        return teamRepository.save(team);
    }

    public void delete(UUID id) {
        if (!teamRepository.existsById(id)) {
            throw new NotFoundException("Team not found");
        }
        teamRepository.deleteById(id);
    }

    private void applyRequest(Team team, TeamDto.TeamRequest request) {
        team.setName(request.name());
        if (request.clubId() != null) {
            Club club = clubRepository.findById(request.clubId())
                    .orElseThrow(() -> new NotFoundException("Club not found"));
            team.setClub(club);
        } else {
            team.setClub(null);
        }
        team.setCoachName(request.coachName());
        team.setHomeGround(request.homeGround());
        team.setLogoUrl(request.logoUrl());
    }
}
