package com.sportsms.club;

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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {
    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @GetMapping
    public List<ClubDto.ClubResponse> list() {
        return clubService.findAll().stream()
                .map(club -> new ClubDto.ClubResponse(club.getId(), club.getName(), club.getCity(), club.getCreatedAt()))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ClubDto.ClubResponse create(@Valid @RequestBody ClubDto.ClubRequest request) {
        Club club = clubService.create(request);
        return new ClubDto.ClubResponse(club.getId(), club.getName(), club.getCity(), club.getCreatedAt());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ClubDto.ClubResponse update(@PathVariable UUID id, @Valid @RequestBody ClubDto.ClubRequest request) {
        Club club = clubService.update(id, request);
        return new ClubDto.ClubResponse(club.getId(), club.getName(), club.getCity(), club.getCreatedAt());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        clubService.delete(id);
    }
}
