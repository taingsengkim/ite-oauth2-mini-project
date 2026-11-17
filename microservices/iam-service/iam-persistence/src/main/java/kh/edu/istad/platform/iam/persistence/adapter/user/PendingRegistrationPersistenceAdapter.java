package kh.edu.istad.platform.iam.persistence.adapter.user;

import kh.edu.istad.platform.iam.port.out.user.PendingRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class PendingRegistrationPersistenceAdapter implements PendingRegistrationRepository {
    private final NamedParameterJdbcTemplate jdbc;
    @Override public boolean allow(String key, int limit, Duration window, Instant now) {
        Integer count = jdbc.queryForObject("""
                INSERT INTO registration_limits(key_hash, expires_at, request_count) VALUES (:key,:expires,1)
                ON CONFLICT(key_hash) DO UPDATE SET
                  expires_at = CASE WHEN registration_limits.expires_at <= :now THEN :expires ELSE registration_limits.expires_at END,
                  request_count = CASE WHEN registration_limits.expires_at <= :now THEN 1 ELSE LEAST(registration_limits.request_count + 1,1000000) END
                RETURNING request_count
                """, new MapSqlParameterSource().addValue("key",key).addValue("now",Timestamp.from(now))
                .addValue("expires",Timestamp.from(now.plus(window))), Integer.class);
        return count != null && count <= limit;
    }
    @Override public boolean insert(Pending p) {
        return jdbc.update("""
                INSERT INTO pending_registrations(token_hash,email,username,password_hash,given_name,family_name,locale,code_hash,created_at,expires_at,sent_at,attempts)
                VALUES(:token,:email,:username,:password,:given,:family,:locale,:code,:created,:expires,:sent,0)
                ON CONFLICT(email) DO NOTHING
                """, new MapSqlParameterSource().addValue("token",p.tokenHash()).addValue("email",p.email())
                .addValue("username",p.username()).addValue("password",p.passwordHash()).addValue("given",p.givenName())
                .addValue("family",p.familyName()).addValue("locale",p.locale()).addValue("code",p.codeHash())
                .addValue("created",Timestamp.from(p.createdAt())).addValue("expires",Timestamp.from(p.expiresAt()))
                .addValue("sent",Timestamp.from(p.sentAt()))) == 1;
    }
    @Override public Optional<Pending> lock(String hash) {
        return jdbc.query("SELECT * FROM pending_registrations WHERE token_hash=:hash FOR UPDATE",Map.of("hash",hash),
                (r,n) -> new Pending(r.getString("token_hash"),r.getString("email"),r.getString("username"),r.getString("password_hash"),
                        r.getString("given_name"),r.getString("family_name"),r.getString("locale"),r.getString("code_hash"),
                        r.getTimestamp("created_at").toInstant(),r.getTimestamp("expires_at").toInstant(),r.getTimestamp("sent_at").toInstant(),r.getInt("attempts"))).stream().findFirst();
    }
    @Override public void failedAttempt(String hash) { jdbc.update("UPDATE pending_registrations SET attempts=LEAST(attempts+1,5) WHERE token_hash=:hash",Map.of("hash",hash)); }
    @Override public void replaceCode(String hash,String code,Instant sent,Instant expires) {
        jdbc.update("UPDATE pending_registrations SET code_hash=:code,sent_at=:sent,expires_at=:expires WHERE token_hash=:hash",
                new MapSqlParameterSource().addValue("hash",hash).addValue("code",code).addValue("sent",Timestamp.from(sent)).addValue("expires",Timestamp.from(expires)));
    }
    @Override public void remove(String hash) { jdbc.update("DELETE FROM pending_registrations WHERE token_hash=:hash",Map.of("hash",hash)); }
    @Override public void revokeDelivery(String hash,String code) { jdbc.update("UPDATE pending_registrations SET code_hash=repeat('0',64) WHERE token_hash=:hash AND code_hash=:code",Map.of("hash",hash,"code",code)); }
    @Override public void cleanup(Instant now) {
        var values=Map.of("now",Timestamp.from(now));
        jdbc.update("DELETE FROM pending_registrations WHERE expires_at<=:now",values);
        jdbc.update("DELETE FROM registration_limits WHERE expires_at<=:now",values);
    }
}
