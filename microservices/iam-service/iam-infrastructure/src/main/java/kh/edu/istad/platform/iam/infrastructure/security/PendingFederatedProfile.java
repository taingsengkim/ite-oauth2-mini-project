package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.dto.identity.FederatedIdentityCommand;
import java.io.Serializable;
import java.time.Instant;

public record PendingFederatedProfile(String proofToken, String registrationId,
                                     FederatedIdentityCommand identity, Instant expiresAt) implements Serializable {
    @Override public String toString() { return "PendingFederatedProfile[redacted]"; }
}
