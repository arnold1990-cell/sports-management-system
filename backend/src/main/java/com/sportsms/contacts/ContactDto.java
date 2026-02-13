package com.sportsms.contacts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ContactDto {
    public record HeadOfficeRequest(@NotBlank String name, String address, String phone, String email, String region, String logoUrl) {}
    public record HeadOfficeResponse(UUID id, String name, String address, String phone, String email, String region, String logoUrl) {}
    public record OrganizationRoleRequest(@NotNull UUID headOfficeId, @NotBlank String roleName, @NotBlank String personName,
                                          String phone, String email, LocalDate termStart, LocalDate termEnd) {}
    public record OrganizationRoleResponse(UUID id, UUID headOfficeId, String roleName, String personName,
                                           String phone, String email, LocalDate termStart, LocalDate termEnd) {}
    public record ClubContactRequest(@NotNull UUID clubId, @NotBlank String adminName, String adminPhone, String adminEmail,
                                     String emergencyContactName, String emergencyContactPhone) {}
    public record ClubContactResponse(UUID id, UUID clubId, String adminName, String adminPhone, String adminEmail,
                                      String emergencyContactName, String emergencyContactPhone, Instant updatedAt) {}
}
