package com.sportsms.contacts;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "organization_roles")
public class OrganizationRole {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_office_id", nullable = false)
    private HeadOffice headOffice;
    @Column(nullable = false)
    private String roleName;
    @Column(nullable = false)
    private String personName;
    private String phone;
    private String email;
    private LocalDate termStart;
    private LocalDate termEnd;
    @PrePersist
    public void init() { if (id == null) id = UUID.randomUUID(); }
    public UUID getId() { return id; }
    public HeadOffice getHeadOffice() { return headOffice; }
    public void setHeadOffice(HeadOffice headOffice) { this.headOffice = headOffice; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getTermStart() { return termStart; }
    public void setTermStart(LocalDate termStart) { this.termStart = termStart; }
    public LocalDate getTermEnd() { return termEnd; }
    public void setTermEnd(LocalDate termEnd) { this.termEnd = termEnd; }
}
