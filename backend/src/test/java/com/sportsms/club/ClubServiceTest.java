package com.sportsms.club;

import com.sportsms.common.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {
    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ClubService clubService;

    @Test
    void createPersistsClubDetails() {
        ClubDto.ClubRequest request = new ClubDto.ClubRequest("Falcons FC", "Accra");
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Club result = clubService.create(request);

        Assertions.assertEquals("Falcons FC", result.getName());
        Assertions.assertEquals("Accra", result.getCity());
        verify(clubRepository).save(result);
    }

    @Test
    void updateThrowsWhenMissing() {
        UUID clubId = UUID.randomUUID();
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,
                () -> clubService.update(clubId, new ClubDto.ClubRequest("Name", "City")));
    }

    @Test
    void deleteThrowsWhenMissing() {
        UUID clubId = UUID.randomUUID();
        when(clubRepository.existsById(clubId)).thenReturn(false);

        Assertions.assertThrows(NotFoundException.class, () -> clubService.delete(clubId));
    }
}
