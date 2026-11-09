
package kh.edu.istad.platform.iam.rest.identity.dto;

import kh.edu.istad.platform.iam.model.IdentityProvider;

import java.time.Instant;
import java.util.UUID;

public record IdentityResponse(
        UUID uuid,
        IdentityProvider provider,
        String email,
        boolean emailVerified,
        String displayName,
        String username,
        String pictureUrl,
        Instant linkedAt,
        Instant lastLoginAt
) {
}
