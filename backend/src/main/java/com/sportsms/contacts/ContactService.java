package com.sportsms.contacts;

import com.sportsms.club.ClubRepository;
import com.sportsms.common.NotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ContactService {
    private final HeadOfficeRepository headOfficeRepository;
    private final OrganizationRoleRepository organizationRoleRepository;
    private final ClubContactRepository clubContactRepository;
    private final ClubRepository clubRepository;

    public ContactService(HeadOfficeRepository headOfficeRepository, OrganizationRoleRepository organizationRoleRepository,
                          ClubContactRepository clubContactRepository, ClubRepository clubRepository) {
        this.headOfficeRepository = headOfficeRepository;
        this.organizationRoleRepository = organizationRoleRepository;
        this.clubContactRepository = clubContactRepository;
        this.clubRepository = clubRepository;
    }

    public List<ContactDto.HeadOfficeResponse> listHeadOffices() {
        return headOfficeRepository.findAll().stream().map(h -> new ContactDto.HeadOfficeResponse(h.getId(), h.getName(), h.getAddress(), h.getPhone(), h.getEmail(), h.getRegion(), h.getLogoUrl())).toList();
    }

    public ContactDto.HeadOfficeResponse saveHeadOffice(ContactDto.HeadOfficeRequest request) {
        HeadOffice h = new HeadOffice();
        h.setName(request.name()); h.setAddress(request.address()); h.setPhone(request.phone()); h.setEmail(request.email()); h.setRegion(request.region()); h.setLogoUrl(request.logoUrl());
        h = headOfficeRepository.save(h);
        return new ContactDto.HeadOfficeResponse(h.getId(), h.getName(), h.getAddress(), h.getPhone(), h.getEmail(), h.getRegion(), h.getLogoUrl());
    }

    public List<ContactDto.OrganizationRoleResponse> listRoles(UUID headOfficeId) {
        return organizationRoleRepository.findByHeadOfficeId(headOfficeId).stream().map(r -> new ContactDto.OrganizationRoleResponse(r.getId(), r.getHeadOffice().getId(), r.getRoleName(), r.getPersonName(), r.getPhone(), r.getEmail(), r.getTermStart(), r.getTermEnd())).toList();
    }

    public ContactDto.OrganizationRoleResponse addRole(ContactDto.OrganizationRoleRequest request) {
        HeadOffice h = headOfficeRepository.findById(request.headOfficeId()).orElseThrow(() -> new NotFoundException("Head office not found"));
        OrganizationRole r = new OrganizationRole();
        r.setHeadOffice(h); r.setRoleName(request.roleName()); r.setPersonName(request.personName()); r.setPhone(request.phone()); r.setEmail(request.email()); r.setTermStart(request.termStart()); r.setTermEnd(request.termEnd());
        r = organizationRoleRepository.save(r);
        return new ContactDto.OrganizationRoleResponse(r.getId(), h.getId(), r.getRoleName(), r.getPersonName(), r.getPhone(), r.getEmail(), r.getTermStart(), r.getTermEnd());
    }

    public List<ContactDto.ClubContactResponse> listClubContacts() {
        return clubContactRepository.findAll().stream().map(c -> new ContactDto.ClubContactResponse(c.getId(), c.getClub().getId(), c.getAdminName(), c.getAdminPhone(), c.getAdminEmail(), c.getEmergencyContactName(), c.getEmergencyContactPhone(), c.getUpdatedAt())).toList();
    }

    public ContactDto.ClubContactResponse upsertClubContact(ContactDto.ClubContactRequest request) {
        var club = clubRepository.findById(request.clubId()).orElseThrow(() -> new NotFoundException("Club not found"));
        ClubContact c = clubContactRepository.findByClubId(request.clubId()).orElseGet(ClubContact::new);
        c.setClub(club); c.setAdminName(request.adminName()); c.setAdminPhone(request.adminPhone()); c.setAdminEmail(request.adminEmail());
        c.setEmergencyContactName(request.emergencyContactName()); c.setEmergencyContactPhone(request.emergencyContactPhone()); c.setUpdatedAt(Instant.now());
        c = clubContactRepository.save(c);
        return new ContactDto.ClubContactResponse(c.getId(), c.getClub().getId(), c.getAdminName(), c.getAdminPhone(), c.getAdminEmail(), c.getEmergencyContactName(), c.getEmergencyContactPhone(), c.getUpdatedAt());
    }
}
