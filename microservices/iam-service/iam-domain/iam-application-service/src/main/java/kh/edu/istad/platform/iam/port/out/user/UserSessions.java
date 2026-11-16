package kh.edu.istad.platform.iam.port.out.user;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserSessions {
    record Session(UUID id, Instant signedInAt, Instant lastActiveAt, Instant expiresAt,
                   String ipAddress, String userAgent, boolean current, IpLocationResolver.Location location) {}
    List<Session> list(UUID userUuid, String currentSessionId);
    void terminate(UUID userUuid, UUID sessionId);
    void terminateAll(UUID userUuid);
}
