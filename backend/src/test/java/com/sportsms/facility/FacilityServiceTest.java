package com.sportsms.facility;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.sportsms.club.ClubRepository;
import com.sportsms.user.UserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FacilityServiceTest {
    @Test
    void detectsBookingConflict() {
        FacilityRepository facilities = Mockito.mock(FacilityRepository.class);
        FacilityBookingRepository bookings = Mockito.mock(FacilityBookingRepository.class);
        MaintenanceScheduleRepository maintenance = Mockito.mock(MaintenanceScheduleRepository.class);
        UserRepository users = Mockito.mock(UserRepository.class);
        ClubRepository clubs = Mockito.mock(ClubRepository.class);
        FacilityService service = new FacilityService(facilities, bookings, maintenance, users, clubs);

        UUID id = UUID.randomUUID();
        when(facilities.findById(id)).thenReturn(Optional.of(new Facility()));
        when(users.findById(any())).thenReturn(Optional.of(new com.sportsms.user.User()));
        when(bookings.findByFacilityIdAndEndDateTimeAfterAndStartDateTimeBefore(eq(id), any(), any())).thenReturn(List.of(new FacilityBooking()));

        assertThrows(IllegalArgumentException.class, () -> service.createBooking(new FacilityBookingRequest(id, UUID.randomUUID(), null, OffsetDateTime.now(), OffsetDateTime.now().plusHours(1), false, "")));
    }
}
