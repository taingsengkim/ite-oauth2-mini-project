package kh.edu.istad.platform.iam.persistence.adapter.user;

import kh.edu.istad.platform.iam.port.out.user.PasswordResetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class PasswordResetPersistenceAdapter implements PasswordResetRepository {
    private final NamedParameterJdbcTemplate jdbc;
    @Override public boolean allow(String key, int limit, Duration window, Instant now) {
        var params = new MapSqlParameterSource().addValue("key", key).addValue("now", Timestamp.from(now))
                .addValue("expires", Timestamp.from(now.plus(window)));
        Integer count = jdbc.queryForObject("""
            INSERT INTO password_reset_limits (key_hash, window_start, expires_at, request_count) VALUES (:key, :now, :expires, 1)
            ON CONFLICT (key_hash) DO UPDATE SET
                window_start = CASE WHEN password_reset_limits.expires_at <= :now THEN :now ELSE password_reset_limits.window_start END,
                expires_at = CASE WHEN password_reset_limits.expires_at <= :now THEN :expires ELSE password_reset_limits.expires_at END,
                request_count = CASE WHEN password_reset_limits.expires_at <= :now THEN 1 ELSE LEAST(password_reset_limits.request_count + 1, 1000000) END
            RETURNING request_count
            """, params, Integer.class);
        return count != null && count <= limit;
    }
    @Override public Optional<Token> find(String hash, boolean forUpdate) {
        return jdbc.query("SELECT * FROM password_reset_tokens WHERE token_hash = :hash" + (forUpdate ? " FOR UPDATE" : ""),
                Map.of("hash", hash), (rs, row) -> new Token(rs.getString("token_hash"), rs.getObject("user_uuid", UUID.class),
                    rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("expires_at").toInstant())).stream().findFirst();
    }
    @Override public boolean recentlyIssued(UUID user, Instant since) {
        Long count = jdbc.queryForObject("SELECT count(*) FROM password_reset_tokens WHERE user_uuid = :user AND created_at > :since",
                new MapSqlParameterSource().addValue("user", user).addValue("since", Timestamp.from(since)), Long.class);
        return count != null && count > 0;
    }
    @Override public void issue(Token token) {
        revokeUser(token.userUuid());
        jdbc.update("INSERT INTO password_reset_tokens (token_hash,user_uuid,created_at,expires_at) VALUES (:hash,:user,:created,:expires)",
                new MapSqlParameterSource().addValue("hash", token.hash()).addValue("user", token.userUuid())
                    .addValue("created", Timestamp.from(token.createdAt())).addValue("expires", Timestamp.from(token.expiresAt())));
    }
    @Override public void revokeUser(UUID user) { jdbc.update("DELETE FROM password_reset_tokens WHERE user_uuid = :user", Map.of("user", user)); }
    @Override public void revoke(String hash) { jdbc.update("DELETE FROM password_reset_tokens WHERE token_hash = :hash", Map.of("hash", hash)); }
    @Override public void cleanup(Instant now) {
        var params = Map.of("now", Timestamp.from(now));
        jdbc.update("DELETE FROM password_reset_tokens WHERE expires_at <= :now", params);
        jdbc.update("DELETE FROM password_reset_limits WHERE expires_at <= :now", params);
    }
}
