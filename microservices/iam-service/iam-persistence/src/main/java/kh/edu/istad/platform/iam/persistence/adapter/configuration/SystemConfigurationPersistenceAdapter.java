package kh.edu.istad.platform.iam.persistence.adapter.configuration;

import kh.edu.istad.platform.iam.dto.configuration.SecurityPolicy;
import kh.edu.istad.platform.iam.port.out.configuration.SystemConfigurationRepository;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class SystemConfigurationPersistenceAdapter implements SystemConfigurationRepository {
    private final JdbcOperations jdbc;
    @Override public Snapshot current() {
        return jdbc.queryForObject("SELECT * FROM system_configuration WHERE id = 1", (rs, row) -> new Snapshot("system",
            new SecurityPolicy(rs.getInt("max_failed_login_attempts"), rs.getInt("lockout_duration_seconds"), rs.getInt("password_min_length"), rs.getInt("password_max_length"),
                rs.getBoolean("password_require_uppercase"), rs.getBoolean("password_require_lowercase"), rs.getBoolean("password_require_digit"), rs.getBoolean("password_require_special"),
                rs.getInt("session_idle_timeout_seconds"), rs.getLong("default_access_token_ttl_seconds"), rs.getLong("default_refresh_token_ttl_seconds")),
            rs.getLong("version"), rs.getTimestamp("updated_at").toInstant(), rs.getString("updated_by")));
    }
    @Override public Snapshot update(SecurityPolicy p, long expectedVersion, String actor, Instant now) {
        int updated = jdbc.update("""
            UPDATE system_configuration SET max_failed_login_attempts=?, lockout_duration_seconds=?, password_min_length=?, password_max_length=?,
                password_require_uppercase=?, password_require_lowercase=?, password_require_digit=?, password_require_special=?,
                session_idle_timeout_seconds=?, default_access_token_ttl_seconds=?, default_refresh_token_ttl_seconds=?,
                version=version+1, updated_at=?, updated_by=? WHERE id=1 AND version=?
            """, p.maxFailedLoginAttempts(), p.lockoutDurationSeconds(), p.passwordMinLength(), p.passwordMaxLength(), p.passwordRequireUppercase(),
                p.passwordRequireLowercase(), p.passwordRequireDigit(), p.passwordRequireSpecial(), p.sessionIdleTimeoutSeconds(),
                p.defaultAccessTokenTtlSeconds(), p.defaultRefreshTokenTtlSeconds(), Timestamp.from(now), actor, expectedVersion);
        if (updated != 1) throw new ResourceConflictException("Configuration changed in another session. Reload before saving.");
        return current();
    }
}
