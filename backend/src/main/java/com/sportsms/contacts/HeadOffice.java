package com.sportsms.contacts;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "head_offices")
public class HeadOffice {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String name;
    private String address;
    private String phone;
    private String email;
    private String region;
    private String logoUrl;

    @PrePersist
    public void init() { if (id == null) id = UUID.randomUUID(); }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
}
