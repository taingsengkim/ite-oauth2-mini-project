--liquibase formatted sql

--changeset kim:022-system-configuration
CREATE TABLE system_configuration (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    max_failed_login_attempts INTEGER NOT NULL CHECK (max_failed_login_attempts BETWEEN 3 AND 20),
    lockout_duration_seconds INTEGER NOT NULL CHECK (lockout_duration_seconds BETWEEN 60 AND 86400),
    password_min_length INTEGER NOT NULL CHECK (password_min_length BETWEEN 8 AND 64),
    password_max_length INTEGER NOT NULL CHECK (password_max_length BETWEEN password_min_length AND 72),
    password_require_uppercase BOOLEAN NOT NULL,
    password_require_lowercase BOOLEAN NOT NULL,
    password_require_digit BOOLEAN NOT NULL,
    password_require_special BOOLEAN NOT NULL,
    session_idle_timeout_seconds INTEGER NOT NULL CHECK (session_idle_timeout_seconds BETWEEN 60 AND 86400),
    default_access_token_ttl_seconds BIGINT NOT NULL CHECK (default_access_token_ttl_seconds BETWEEN 60 AND 86400),
    default_refresh_token_ttl_seconds BIGINT NOT NULL CHECK (default_refresh_token_ttl_seconds BETWEEN 300 AND 2592000 AND default_refresh_token_ttl_seconds >= default_access_token_ttl_seconds),
    version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(100) NOT NULL
);
INSERT INTO system_configuration VALUES (1, 5, 900, 8, 72, FALSE, FALSE, FALSE, FALSE, 1800, 3600, 604800, 0, CURRENT_TIMESTAMP, 'system');

-- Automatic password lockouts are separate from manual administrator account locks.
CREATE TABLE login_lockouts (
    user_uuid UUID PRIMARY KEY REFERENCES users(uuid),
    failed_attempts INTEGER NOT NULL CHECK (failed_attempts >= 1),
    locked_until TIMESTAMPTZ
);

INSERT INTO permissions (uuid, name, resource, action, description, is_system, is_active, is_deleted,
                         created_at, created_by, updated_at, updated_by, version)
SELECT gen_random_uuid(), 'configuration:' || action, 'configuration', action, 'IAM system configuration ' || action,
       TRUE, TRUE, FALSE, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 0
FROM (VALUES ('read'), ('update')) AS definitions(action)
ON CONFLICT (name) DO UPDATE SET resource = 'configuration', action = EXCLUDED.action, is_system = TRUE,
    is_active = TRUE, is_deleted = FALSE, deleted_at = NULL, deleted_by = NULL,
    updated_at = CURRENT_TIMESTAMP, updated_by = 'system', version = permissions.version + 1;

UPDATE role_permissions assignment SET revoked_at = CURRENT_TIMESTAMP, revoked_by = 'system',
    reason = 'Expired configuration assignment replaced', updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system', version = assignment.version + 1
FROM roles role, permissions permission
WHERE assignment.role_id = role.id AND assignment.permission_id = permission.id
    AND role.name = 'ADMIN' AND permission.resource = 'configuration'
    AND assignment.revoked_at IS NULL AND assignment.is_deleted = FALSE AND assignment.expires_at <= CURRENT_TIMESTAMP;

INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by, reason,
                              created_at, created_by, updated_at, updated_by, version)
SELECT role.id, permission.id, CURRENT_TIMESTAMP, 'system', 'System configuration administrator access',
       CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 0
FROM roles role CROSS JOIN permissions permission
WHERE role.name = 'ADMIN' AND permission.resource = 'configuration' AND permission.is_active = TRUE AND permission.is_deleted = FALSE
ON CONFLICT DO NOTHING;
