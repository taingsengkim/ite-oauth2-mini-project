package kh.edu.istad.platform.iam.port.out.user;

import java.time.Instant;
import java.util.UUID;

public interface LoginLockoutRepository {
    boolean isLocked(UUID userUuid, Instant now);
    void recordFailure(UUID userUuid, int threshold, int durationSeconds, Instant now);
    void clear(UUID userUuid);
}
