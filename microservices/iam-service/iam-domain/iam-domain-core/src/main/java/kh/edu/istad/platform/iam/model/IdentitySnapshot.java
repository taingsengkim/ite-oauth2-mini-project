package kh.edu.istad.platform.iam.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record IdentitySnapshot(
        UUID uuid,
        UUID userUuid,
        IdentityProvider provider,
        String providerSubject,
        String email,
        boolean emailVerified,
        String displayName,
        String username,
        String pictureUrl,
        Map<String, Object> attributes,
        Instant linkedAt,
        Instant lastLoginAt,
        boolean deleted,
        Instant deletedAt,
        String deletedBy,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {
}