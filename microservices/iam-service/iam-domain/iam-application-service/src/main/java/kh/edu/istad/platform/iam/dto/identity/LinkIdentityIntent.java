package kh.edu.istad.platform.iam.dto.identity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record LinkIdentityIntent(
        UUID userUuid,
        String registrationId,
        Instant expiresAt,
        boolean popup
) implements Serializable {
    public LinkIdentityIntent(UUID userUuid, String registrationId, Instant expiresAt) {
        this(userUuid, registrationId, expiresAt, false);
    }
}
