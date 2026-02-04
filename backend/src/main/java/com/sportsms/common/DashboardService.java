package com.sportsms.common;

import com.sportsms.club.ClubRepository;
import com.sportsms.fixture.FixtureRepository;
import com.sportsms.player.PlayerRepository;
import com.sportsms.post.PostRepository;
import com.sportsms.team.TeamRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final ClubRepository clubRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final FixtureRepository fixtureRepository;
    private final PostRepository postRepository;

    public DashboardService(ClubRepository clubRepository,
                            TeamRepository teamRepository,
                            PlayerRepository playerRepository,
                            FixtureRepository fixtureRepository,
                            PostRepository postRepository) {
        this.clubRepository = clubRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.fixtureRepository = fixtureRepository;
        this.postRepository = postRepository;
    }

    public DashboardDto.DashboardResponse getDashboard() {
        DashboardDto.Summary summary = new DashboardDto.Summary(
                clubRepository.count(),
                teamRepository.count(),
                playerRepository.count(),
                fixtureRepository.count(),
                postRepository.count());
        var fixtureCards = fixtureRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "matchDate")))
                .map(fixture -> new DashboardDto.FixtureCard(
                        fixture.getId(),
                        fixture.getHomeTeam().getName(),
                        fixture.getAwayTeam().getName(),
                        fixture.getMatchDate(),
                        fixture.getStatus(),
                        fixture.getHomeScore(),
                        fixture.getAwayScore()))
                .toList();
        List<String> latestPosts = postRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(post -> post.getTitle())
                .toList();
        return new DashboardDto.DashboardResponse(summary, fixtureCards, latestPosts);
    }
}
