package kh.edu.istad.platform.iam.port.out.user;

import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import java.time.Instant;
import java.util.UUID;

public interface LoginHistoryRepository {
    enum Outcome { SUCCESS, FAILURE }
    record Event(UUID uuid, UUID userUuid, String attemptedLogin, String provider, Outcome outcome,
                 String reason, Instant occurredAt, String ipAddress, String userAgent, boolean suspicious) {}
    record Query(UUID userUuid, String login, Outcome outcome, Instant from, Instant to,
                 boolean suspiciousOnly, int page, int size) {}
    void append(Event event);
    PageResult<Event> findAll(Query query);
}
