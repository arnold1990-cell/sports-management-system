package com.sportsms.contacts;

import com.sportsms.club.Club;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "club_contacts")
public class ClubContact {
    @Id
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false, unique = true)
    private Club club;
    @Column(nullable = false)
    private String adminName;
    private String adminPhone;
    private String adminEmail;
    private String emergencyContactName;
    private String emergencyContactPhone;
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void init() { if (id == null) id = UUID.randomUUID(); if (updatedAt == null) updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public Club getClub() { return club; }
    public void setClub(Club club) { this.club = club; }
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public String getAdminPhone() { return adminPhone; }
    public void setAdminPhone(String adminPhone) { this.adminPhone = adminPhone; }
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
