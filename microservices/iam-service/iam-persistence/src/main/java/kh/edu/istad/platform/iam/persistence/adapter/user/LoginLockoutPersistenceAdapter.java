package kh.edu.istad.platform.iam.persistence.adapter.user;

import kh.edu.istad.platform.iam.port.out.user.LoginLockoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LoginLockoutPersistenceAdapter implements LoginLockoutRepository {
    private final NamedParameterJdbcTemplate jdbc;
    @Override public boolean isLocked(UUID user, Instant now) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM login_lockouts WHERE user_uuid=:user AND locked_until>:now)",
            new MapSqlParameterSource("user", user).addValue("now", Timestamp.from(now)), Boolean.class));
    }
    @Override public void recordFailure(UUID user, int threshold, int duration, Instant now) {
        jdbc.update("""
            INSERT INTO login_lockouts (user_uuid, failed_attempts, locked_until) VALUES (:user, 1, NULL)
            ON CONFLICT (user_uuid) DO UPDATE SET
                failed_attempts = CASE WHEN login_lockouts.locked_until <= :now THEN 1 ELSE LEAST(login_lockouts.failed_attempts + 1, :limit) END,
                locked_until = CASE WHEN login_lockouts.locked_until > :now THEN login_lockouts.locked_until
                    WHEN login_lockouts.locked_until <= :now THEN NULL
                    WHEN login_lockouts.failed_attempts + 1 >= :limit THEN :until ELSE NULL END
            """, new MapSqlParameterSource("user", user).addValue("now", Timestamp.from(now)).addValue("until", Timestamp.from(now.plusSeconds(duration))).addValue("limit", threshold));
    }
    @Override public void clear(UUID user) { jdbc.update("DELETE FROM login_lockouts WHERE user_uuid=:user", new MapSqlParameterSource("user", user)); }
}
