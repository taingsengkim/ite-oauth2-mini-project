
package kh.edu.istad.platform.iam.dto.identity;

import kh.edu.istad.platform.iam.model.IdentityProvider;

import java.time.Instant;
import java.util.UUID;

public record IdentityResult(
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
