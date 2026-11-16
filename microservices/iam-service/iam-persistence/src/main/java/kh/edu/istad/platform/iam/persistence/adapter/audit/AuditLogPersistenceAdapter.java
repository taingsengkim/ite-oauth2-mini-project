package kh.edu.istad.platform.iam.persistence.adapter.audit;

import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.port.out.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class AuditLogPersistenceAdapter implements AuditLogRepository {
    private final NamedParameterJdbcTemplate jdbc;
    // Existing login history participates directly, including events recorded before this feature.
    private static final String EVENTS = """
        WITH events AS (
            SELECT uuid, occurred_at, actor, actor_uuid, action, resource, target_id, outcome,
                   ip_address, user_agent, related_ids, submitted_fields, expires_at, effect,
                   NULL::varchar AS provider, NULL::varchar AS reason FROM audit_logs
            UNION ALL
            SELECT uuid, occurred_at, COALESCE(attempted_login, 'Anonymous') AS actor,
                   CASE WHEN outcome = 'SUCCESS' THEN user_uuid ELSE NULL END AS actor_uuid,
                   CASE WHEN outcome = 'SUCCESS' THEN 'login.success' ELSE 'login.failure' END AS action,
                   'authentication' AS resource, user_uuid::text AS target_id, outcome, ip_address, user_agent,
                   ARRAY[]::text[] AS related_ids, ARRAY[]::text[] AS submitted_fields,
                   NULL::timestamptz AS expires_at, NULL::varchar AS effect, provider, reason
            FROM user_login_history
        )
        """;

    @Override public void append(Event event) {
        jdbc.getJdbcTemplate().execute((ConnectionCallback<Void>) connection -> {
            var related = connection.createArrayOf("text", event.relatedIds().toArray(String[]::new));
            var fields = connection.createArrayOf("text", event.submittedFields().toArray(String[]::new));
            try (var statement = connection.prepareStatement("""
                INSERT INTO audit_logs (uuid, occurred_at, actor, actor_uuid, action, resource, target_id,
                    outcome, ip_address, user_agent, related_ids, submitted_fields, expires_at, effect)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
                statement.setObject(1, event.uuid()); statement.setTimestamp(2, Timestamp.from(event.occurredAt()));
                statement.setString(3, event.actor()); statement.setObject(4, event.actorUuid());
                statement.setString(5, event.action()); statement.setString(6, event.resource()); statement.setString(7, event.targetId());
                statement.setString(8, event.outcome().name()); statement.setString(9, event.ipAddress()); statement.setString(10, event.userAgent());
                statement.setArray(11, related); statement.setArray(12, fields);
                statement.setTimestamp(13, event.expiresAt() == null ? null : Timestamp.from(event.expiresAt())); statement.setString(14, event.effect());
                statement.executeUpdate();
            } finally { related.free(); fields.free(); }
            return null;
        });
    }

    @Override public PageResult<Event> findAll(Query query) {
        var where = new StringBuilder(" WHERE true");
        var params = new MapSqlParameterSource();
        if (!blank(query.actor())) {
            where.append(" AND (lower(actor) = lower(:actor) OR actor_uuid::text = :actor)"); params.addValue("actor", query.actor().strip());
        }
        if (!blank(query.action())) { where.append(" AND action = :action"); params.addValue("action", query.action()); }
        if (!blank(query.resource())) { where.append(" AND resource = :resource"); params.addValue("resource", query.resource()); }
        if (!blank(query.targetId())) { where.append(" AND target_id = :target"); params.addValue("target", query.targetId().strip()); }
        if (query.outcome() != null) { where.append(" AND outcome = :outcome"); params.addValue("outcome", query.outcome().name()); }
        if (query.from() != null) { where.append(" AND occurred_at >= :from"); params.addValue("from", Timestamp.from(query.from())); }
        if (query.to() != null) { where.append(" AND occurred_at <= :to"); params.addValue("to", Timestamp.from(query.to())); }
        Long count = jdbc.queryForObject(EVENTS + "SELECT count(*) FROM events" + where, params, Long.class);
        params.addValue("size", query.size()).addValue("offset", (long) query.page() * query.size());
        var content = jdbc.query(EVENTS + "SELECT * FROM events" + where + " ORDER BY occurred_at DESC, uuid DESC LIMIT :size OFFSET :offset", params,
                (rs, row) -> new Event(rs.getObject("uuid", UUID.class), rs.getTimestamp("occurred_at").toInstant(), rs.getString("actor"),
                        rs.getObject("actor_uuid", UUID.class), rs.getString("action"), rs.getString("resource"), rs.getString("target_id"),
                        Outcome.valueOf(rs.getString("outcome")), rs.getString("ip_address"), rs.getString("user_agent"),
                        array(rs.getArray("related_ids")), array(rs.getArray("submitted_fields")),
                        rs.getTimestamp("expires_at") == null ? null : rs.getTimestamp("expires_at").toInstant(),
                        rs.getString("effect"), rs.getString("provider"), rs.getString("reason")));
        long total = count == null ? 0 : count;
        return new PageResult<>(content, query.page(), query.size(), total, (int) Math.ceil((double) total / query.size()));
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private List<String> array(java.sql.Array value) throws java.sql.SQLException {
        try { return List.of((String[]) value.getArray()); } finally { value.free(); }
    }
}
