package kh.edu.istad.platform.iam.port.out.user;

import java.time.*;
import java.util.*;

public interface PendingRegistrationRepository {
    record Pending(String tokenHash, String email, String username, String passwordHash,
                   String givenName, String familyName, String locale, String codeHash,
                   Instant createdAt, Instant expiresAt, Instant sentAt, int attempts) {
        @Override public String toString() { return "PendingRegistration[REDACTED]"; }
    }
    boolean allow(String keyHash, int limit, Duration window, Instant now);
    boolean insert(Pending pending);
    Optional<Pending> lock(String tokenHash);
    void failedAttempt(String tokenHash);
    void replaceCode(String tokenHash, String codeHash, Instant sentAt, Instant expiresAt);
    void remove(String tokenHash);
    void revokeDelivery(String tokenHash, String codeHash);
    void cleanup(Instant now);
}
