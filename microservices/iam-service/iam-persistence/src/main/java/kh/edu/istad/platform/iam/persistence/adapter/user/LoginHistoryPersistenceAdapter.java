package kh.edu.istad.platform.iam.persistence.adapter.user;

import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.port.out.user.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LoginHistoryPersistenceAdapter implements LoginHistoryRepository {
    private final NamedParameterJdbcTemplate jdbc;
    // A signal for investigation, not an automatic lockout decision.
    private static final String SUSPICIOUS = """
        (h.outcome = 'FAILURE' AND (SELECT count(*) FROM user_login_history recent
            WHERE recent.outcome = 'FAILURE'
              AND recent.occurred_at >= h.occurred_at - INTERVAL '10 minutes'
              AND recent.occurred_at <= h.occurred_at
              AND ((h.user_uuid IS NOT NULL AND recent.user_uuid = h.user_uuid)
                OR (h.attempted_login IS NOT NULL AND lower(recent.attempted_login) = lower(h.attempted_login))
                OR (h.ip_address IS NOT NULL AND recent.ip_address = h.ip_address))) >= 5)
        """;

    @Override public void append(Event event) {
        jdbc.update("""
            INSERT INTO user_login_history
                (uuid, user_uuid, attempted_login, provider, outcome, reason, occurred_at, ip_address, user_agent)
            VALUES (:uuid, :user, :login, :provider, :outcome, :reason, :time, :ip, :agent)
            """, new MapSqlParameterSource().addValue("uuid", event.uuid()).addValue("user", event.userUuid())
                .addValue("login", event.attemptedLogin()).addValue("provider", event.provider())
                .addValue("outcome", event.outcome().name()).addValue("reason", event.reason())
                .addValue("time", Timestamp.from(event.occurredAt())).addValue("ip", event.ipAddress()).addValue("agent", event.userAgent()));
    }

    @Override public PageResult<Event> findAll(Query query) {
        StringBuilder where = new StringBuilder(" WHERE true");
        var params = new MapSqlParameterSource();
        if (query.userUuid() != null) { where.append(" AND h.user_uuid = :user"); params.addValue("user", query.userUuid()); }
        if (query.login() != null && !query.login().isBlank()) {
            where.append(" AND lower(h.attempted_login) = lower(:login)"); params.addValue("login", query.login().trim());
        }
        if (query.outcome() != null) { where.append(" AND h.outcome = :outcome"); params.addValue("outcome", query.outcome().name()); }
        if (query.from() != null) { where.append(" AND h.occurred_at >= :from"); params.addValue("from", Timestamp.from(query.from())); }
        if (query.to() != null) { where.append(" AND h.occurred_at <= :to"); params.addValue("to", Timestamp.from(query.to())); }
        if (query.suspiciousOnly()) where.append(" AND ").append(SUSPICIOUS);
        Long total = jdbc.queryForObject("SELECT count(*) FROM user_login_history h" + where, params, Long.class);
        params.addValue("limit", query.size()).addValue("offset", (long) query.page() * query.size());
        var events = jdbc.query("SELECT h.*, " + SUSPICIOUS + " AS suspicious FROM user_login_history h" + where
                + " ORDER BY h.occurred_at DESC, h.uuid DESC LIMIT :limit OFFSET :offset", params, (rs, row) ->
                new Event(rs.getObject("uuid", UUID.class), rs.getObject("user_uuid", UUID.class),
                        rs.getString("attempted_login"), rs.getString("provider"), Outcome.valueOf(rs.getString("outcome")),
                        rs.getString("reason"), rs.getTimestamp("occurred_at").toInstant(), rs.getString("ip_address"),
                        rs.getString("user_agent"), rs.getBoolean("suspicious")));
        long count = total == null ? 0 : total;
        return new PageResult<>(events, query.page(), query.size(), count, (int) Math.ceil((double) count / query.size()));
    }
}
