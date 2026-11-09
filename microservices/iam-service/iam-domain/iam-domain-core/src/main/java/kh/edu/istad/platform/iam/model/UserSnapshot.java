package kh.edu.istad.platform.iam.model;

import kh.edu.istad.platform.iam.valueobject.Address;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserSnapshot(
        UUID uuid,
        String username,
        String email,
        boolean emailVerified,
        String passwordHash,

        String givenName,
        String familyName,
        String middleName,
        String nickname,
        String gender,
        LocalDate birthdate,

        String profileUrl,
        String pictureUrl,
        String websiteUrl,
        String zoneinfo,
        String locale,

        String phoneNumber,
        boolean phoneNumberVerified,
        Address address,

        boolean enabled,
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean credentialsNonExpired,

        int failedLoginAttempts,
        Instant lastLoginAt,
        Instant passwordChangedAt,

        boolean deleted,
        Instant deletedAt,
        String deletedBy,

        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {

    @Override
    public String toString() {
        return "UserSnapshot[uuid=" + uuid + "]";
    }
}
