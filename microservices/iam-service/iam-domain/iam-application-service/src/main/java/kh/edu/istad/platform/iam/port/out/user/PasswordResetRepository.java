package kh.edu.istad.platform.iam.port.out.user;

import java.time.*;
import java.util.*;

public interface PasswordResetRepository {
    record Token(String hash, UUID userUuid, Instant createdAt, Instant expiresAt) {
        @Override public String toString() { return "PasswordResetToken[REDACTED]"; }
    }
    boolean allow(String keyHash, int limit, Duration window, Instant now);
    Optional<Token> find(String hash, boolean forUpdate);
    boolean recentlyIssued(UUID userUuid, Instant since);
    void issue(Token token);
    void revokeUser(UUID userUuid);
    void revoke(String hash);
    void cleanup(Instant now);
}
