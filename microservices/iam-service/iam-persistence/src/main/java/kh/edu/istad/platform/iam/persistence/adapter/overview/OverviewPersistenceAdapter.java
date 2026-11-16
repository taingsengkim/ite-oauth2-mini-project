package kh.edu.istad.platform.iam.persistence.adapter.overview;

import kh.edu.istad.platform.iam.port.out.overview.OverviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OverviewPersistenceAdapter implements OverviewRepository {
    private final JdbcOperations jdbc;
    @Override public Statistics statistics() {
        // One statement gives every counter the same PostgreSQL snapshot.
        return jdbc.queryForObject("""
            WITH user_counts AS (
                SELECT count(*) AS total_users,
                       count(*) FILTER (WHERE enabled AND account_non_locked AND account_non_expired) AS active_users,
                       count(*) FILTER (WHERE NOT enabled) AS disabled_users,
                       count(*) FILTER (WHERE NOT account_non_locked) AS locked_users
                FROM users WHERE is_deleted = FALSE
            )
            SELECT total_users, active_users, disabled_users, locked_users,
                   (SELECT count(*) FROM roles WHERE is_deleted = FALSE) AS roles,
                   (SELECT count(*) FROM permissions WHERE is_deleted = FALSE) AS permissions,
                   (SELECT count(*) FROM oauth2_registered_client) AS clients
            FROM user_counts
            """, (rs, row) -> new Statistics(rs.getLong("total_users"), rs.getLong("active_users"),
                rs.getLong("disabled_users"), rs.getLong("locked_users"), rs.getLong("roles"),
                rs.getLong("permissions"), rs.getLong("clients")));
    }
}
