package kh.edu.istad.platform.iam.dto.identity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record LinkIdentityIntent(
        UUID userUuid,
        String registrationId,
        Instant expiresAt
) implements Serializable {
}
