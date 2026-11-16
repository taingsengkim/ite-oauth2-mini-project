package kh.edu.istad.platform.iam.port.out.audit;

import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository {
    enum Outcome { SUCCESS, FAILURE }
    record Event(UUID uuid, Instant occurredAt, String actor, UUID actorUuid, String action,
                 String resource, String targetId, Outcome outcome, String ipAddress, String userAgent,
                 List<String> relatedIds, List<String> submittedFields, Instant expiresAt,
                 String effect, String provider, String reason) {
        public Event { relatedIds = List.copyOf(relatedIds); submittedFields = List.copyOf(submittedFields); }
    }
    record Query(String actor, String action, String resource, String targetId, Outcome outcome,
                 Instant from, Instant to, int page, int size) {}
    void append(Event event);
    PageResult<Event> findAll(Query query);
}
