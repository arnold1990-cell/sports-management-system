package com.sportsms.contacts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface HeadOfficeRepository extends JpaRepository<HeadOffice, UUID> {}
interface OrganizationRoleRepository extends JpaRepository<OrganizationRole, UUID> {
    List<OrganizationRole> findByHeadOfficeId(UUID headOfficeId);
}
interface ClubContactRepository extends JpaRepository<ClubContact, UUID> {
    Optional<ClubContact> findByClubId(UUID clubId);
}
