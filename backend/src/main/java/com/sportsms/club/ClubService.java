package com.sportsms.club;

import com.sportsms.common.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ClubService {
    private final ClubRepository clubRepository;

    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    public List<Club> findAll() {
        return clubRepository.findAll();
    }

    public Club create(ClubDto.ClubRequest request) {
        Club club = new Club();
        club.setName(request.name());
        club.setCity(request.city());
        return clubRepository.save(club);
    }

    public Club update(UUID id, ClubDto.ClubRequest request) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Club not found"));
        club.setName(request.name());
        club.setCity(request.city());
        return clubRepository.save(club);
    }

    public void delete(UUID id) {
        if (!clubRepository.existsById(id)) {
            throw new NotFoundException("Club not found");
        }
        clubRepository.deleteById(id);
    }
}
