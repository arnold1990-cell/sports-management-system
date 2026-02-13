package com.sportsms.facility;

import com.sportsms.club.ClubRepository;
import com.sportsms.common.NotFoundException;
import com.sportsms.user.UserRepository;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

enum FacilityStatus { ACTIVE, MAINTENANCE, INACTIVE }
enum BookingStatus { PENDING, APPROVED, REJECTED, CANCELLED }

@Entity @Table(name = "facilities")
class Facility { @Id private UUID id; @Column(nullable=false) private String name; private UUID sportId; private String location; private Integer capacity;
    private BigDecimal pricePerHour; @Enumerated(EnumType.STRING) private FacilityStatus status; private UUID ownerClubId; @PrePersist void init(){ if(id==null) id=UUID.randomUUID(); }
    public UUID getId(){return id;} public String getName(){return name;} public void setName(String name){this.name=name;} public UUID getSportId(){return sportId;} public void setSportId(UUID sportId){this.sportId=sportId;} public String getLocation(){return location;} public void setLocation(String location){this.location=location;} public Integer getCapacity(){return capacity;} public void setCapacity(Integer capacity){this.capacity=capacity;} public BigDecimal getPricePerHour(){return pricePerHour;} public void setPricePerHour(BigDecimal pricePerHour){this.pricePerHour=pricePerHour;} public FacilityStatus getStatus(){return status;} public void setStatus(FacilityStatus status){this.status=status;} public UUID getOwnerClubId(){return ownerClubId;} public void setOwnerClubId(UUID ownerClubId){this.ownerClubId=ownerClubId;}}
@Entity @Table(name = "facility_bookings")
class FacilityBooking { @Id private UUID id; private UUID facilityId; private UUID requestedByUserId; private UUID clubId; private OffsetDateTime startDateTime; private OffsetDateTime endDateTime; @Enumerated(EnumType.STRING) private BookingStatus status; private boolean paymentRequired; private UUID paymentId; private String notes; private OffsetDateTime createdAt;
    @PrePersist void init(){ if(id==null) id=UUID.randomUUID(); if(createdAt==null) createdAt=OffsetDateTime.now(); }
    public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public void setFacilityId(UUID facilityId){this.facilityId=facilityId;} public UUID getRequestedByUserId(){return requestedByUserId;} public void setRequestedByUserId(UUID requestedByUserId){this.requestedByUserId=requestedByUserId;} public UUID getClubId(){return clubId;} public void setClubId(UUID clubId){this.clubId=clubId;} public OffsetDateTime getStartDateTime(){return startDateTime;} public void setStartDateTime(OffsetDateTime startDateTime){this.startDateTime=startDateTime;} public OffsetDateTime getEndDateTime(){return endDateTime;} public void setEndDateTime(OffsetDateTime endDateTime){this.endDateTime=endDateTime;} public BookingStatus getStatus(){return status;} public void setStatus(BookingStatus status){this.status=status;} public boolean isPaymentRequired(){return paymentRequired;} public void setPaymentRequired(boolean paymentRequired){this.paymentRequired=paymentRequired;} public UUID getPaymentId(){return paymentId;} public void setPaymentId(UUID paymentId){this.paymentId=paymentId;} public String getNotes(){return notes;} public void setNotes(String notes){this.notes=notes;} public OffsetDateTime getCreatedAt(){return createdAt;}}
@Entity @Table(name = "maintenance_schedules")
class MaintenanceSchedule { @Id private UUID id; private UUID facilityId; private OffsetDateTime startDateTime; private OffsetDateTime endDateTime; private String reason; @PrePersist void init(){ if(id==null) id=UUID.randomUUID(); }
    public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public void setFacilityId(UUID facilityId){this.facilityId=facilityId;} public OffsetDateTime getStartDateTime(){return startDateTime;} public void setStartDateTime(OffsetDateTime startDateTime){this.startDateTime=startDateTime;} public OffsetDateTime getEndDateTime(){return endDateTime;} public void setEndDateTime(OffsetDateTime endDateTime){this.endDateTime=endDateTime;} public String getReason(){return reason;} public void setReason(String reason){this.reason=reason;}}

interface FacilityRepository extends JpaRepository<Facility, UUID> {}
interface FacilityBookingRepository extends JpaRepository<FacilityBooking, UUID> {
    List<FacilityBooking> findByFacilityIdAndEndDateTimeAfterAndStartDateTimeBefore(UUID facilityId, OffsetDateTime start, OffsetDateTime end);
    List<FacilityBooking> findByStartDateTimeBetween(OffsetDateTime start, OffsetDateTime end);
}
interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, UUID> {
    List<MaintenanceSchedule> findByFacilityIdAndEndDateTimeAfterAndStartDateTimeBefore(UUID facilityId, OffsetDateTime start, OffsetDateTime end);
}

record FacilityRequest(String name, UUID sportId, String location, Integer capacity, BigDecimal pricePerHour, FacilityStatus status, UUID ownerClubId) {}
record FacilityBookingRequest(UUID facilityId, UUID requestedByUserId, UUID clubId, OffsetDateTime startDateTime, OffsetDateTime endDateTime, boolean paymentRequired, String notes) {}
record MaintenanceRequest(UUID facilityId, OffsetDateTime startDateTime, OffsetDateTime endDateTime, String reason) {}

@Service class FacilityService {
    private final FacilityRepository facilityRepository; private final FacilityBookingRepository bookingRepository; private final MaintenanceScheduleRepository maintenanceRepository; private final UserRepository userRepository; private final ClubRepository clubRepository;
    FacilityService(FacilityRepository facilityRepository, FacilityBookingRepository bookingRepository, MaintenanceScheduleRepository maintenanceRepository, UserRepository userRepository, ClubRepository clubRepository) { this.facilityRepository = facilityRepository; this.bookingRepository = bookingRepository; this.maintenanceRepository = maintenanceRepository; this.userRepository = userRepository; this.clubRepository = clubRepository; }
    List<Facility> facilities() { return facilityRepository.findAll(); }
    Facility createFacility(FacilityRequest request) { Facility f = new Facility(); f.setName(request.name()); f.setSportId(request.sportId()); f.setLocation(request.location()); f.setCapacity(request.capacity()); f.setPricePerHour(request.pricePerHour()); f.setStatus(request.status()); f.setOwnerClubId(request.ownerClubId()); return facilityRepository.save(f); }
    FacilityBooking createBooking(FacilityBookingRequest request) {
        facilityRepository.findById(request.facilityId()).orElseThrow(() -> new NotFoundException("Facility not found"));
        userRepository.findById(request.requestedByUserId()).orElseThrow(() -> new NotFoundException("User not found"));
        if (request.clubId() != null) clubRepository.findById(request.clubId()).orElseThrow(() -> new NotFoundException("Club not found"));
        if (!bookingRepository.findByFacilityIdAndEndDateTimeAfterAndStartDateTimeBefore(request.facilityId(), request.startDateTime(), request.endDateTime()).isEmpty()) throw new IllegalArgumentException("Booking conflict detected");
        if (!maintenanceRepository.findByFacilityIdAndEndDateTimeAfterAndStartDateTimeBefore(request.facilityId(), request.startDateTime(), request.endDateTime()).isEmpty()) throw new IllegalArgumentException("Facility is under maintenance for selected slot");
        FacilityBooking b = new FacilityBooking(); b.setFacilityId(request.facilityId()); b.setRequestedByUserId(request.requestedByUserId()); b.setClubId(request.clubId()); b.setStartDateTime(request.startDateTime()); b.setEndDateTime(request.endDateTime()); b.setPaymentRequired(request.paymentRequired()); b.setStatus(BookingStatus.PENDING); b.setNotes(request.notes()); return bookingRepository.save(b);
    }
    MaintenanceSchedule createMaintenance(MaintenanceRequest request) { MaintenanceSchedule m = new MaintenanceSchedule(); m.setFacilityId(request.facilityId()); m.setStartDateTime(request.startDateTime()); m.setEndDateTime(request.endDateTime()); m.setReason(request.reason()); return maintenanceRepository.save(m); }
    List<FacilityBooking> bookings(OffsetDateTime start, OffsetDateTime end) { return bookingRepository.findByStartDateTimeBetween(start, end); }
}

@RestController @RequestMapping("/api/facilities")
class FacilityController {
    private final FacilityService service; FacilityController(FacilityService service) { this.service = service; }
    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')") List<Facility> facilities() { return service.facilities(); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") Facility create(@Valid @RequestBody FacilityRequest request) { return service.createFacility(request); }
    @PostMapping("/bookings") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')") FacilityBooking booking(@Valid @RequestBody FacilityBookingRequest request) { return service.createBooking(request); }
    @GetMapping("/bookings") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')") List<FacilityBooking> bookings(@RequestParam OffsetDateTime start, @RequestParam OffsetDateTime end) { return service.bookings(start, end); }
    @PostMapping("/maintenance") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") MaintenanceSchedule maintenance(@Valid @RequestBody MaintenanceRequest request) { return service.createMaintenance(request); }
}
