package com.sportsms.contacts;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) { this.contactService = contactService; }

    @GetMapping("/head-offices")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<ContactDto.HeadOfficeResponse> headOffices() { return contactService.listHeadOffices(); }

    @PostMapping("/head-offices")
    @PreAuthorize("hasRole('ADMIN')")
    public ContactDto.HeadOfficeResponse createHeadOffice(@Valid @RequestBody ContactDto.HeadOfficeRequest request) { return contactService.saveHeadOffice(request); }

    @GetMapping("/organization-roles/{headOfficeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<ContactDto.OrganizationRoleResponse> roles(@PathVariable UUID headOfficeId) { return contactService.listRoles(headOfficeId); }

    @PostMapping("/organization-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ContactDto.OrganizationRoleResponse addRole(@Valid @RequestBody ContactDto.OrganizationRoleRequest request) { return contactService.addRole(request); }

    @GetMapping("/club-directory")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')")
    public List<ContactDto.ClubContactResponse> clubContacts() { return contactService.listClubContacts(); }

    @PutMapping("/club-contact")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ContactDto.ClubContactResponse saveClubContact(@Valid @RequestBody ContactDto.ClubContactRequest request) { return contactService.upsertClubContact(request); }
}
