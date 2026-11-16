--liquibase formatted sql

--changeset kim:020-create-audit-logs
CREATE TABLE audit_logs (
    uuid UUID PRIMARY KEY,
    occurred_at TIMESTAMPTZ NOT NULL,
    actor VARCHAR(255) NOT NULL,
    actor_uuid UUID,
    action VARCHAR(64) NOT NULL,
    resource VARCHAR(30) NOT NULL,
    target_id VARCHAR(100) NOT NULL,
    outcome VARCHAR(10) NOT NULL CHECK (outcome IN ('SUCCESS', 'FAILURE')),
    ip_address VARCHAR(64),
    user_agent VARCHAR(512),
    related_ids TEXT[] NOT NULL DEFAULT '{}',
    submitted_fields TEXT[] NOT NULL DEFAULT '{}',
    expires_at TIMESTAMPTZ,
    effect VARCHAR(5) CHECK (effect IN ('GRANT', 'DENY'))
);
-- No account/resource foreign keys: deleting an account or client must preserve its audit trail.
CREATE INDEX idx_audit_logs_time ON audit_logs (occurred_at DESC, uuid DESC);
CREATE INDEX idx_audit_logs_actor ON audit_logs (lower(actor), occurred_at DESC);
CREATE INDEX idx_audit_logs_actor_uuid ON audit_logs (actor_uuid, occurred_at DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs (action, occurred_at DESC);
CREATE INDEX idx_audit_logs_target ON audit_logs (resource, target_id, occurred_at DESC);

INSERT INTO permissions (uuid, name, resource, action, description, is_system, is_active, is_deleted,
                         created_at, created_by, updated_at, updated_by, version)
VALUES (gen_random_uuid(), 'audit:read', 'audit', 'read', 'Read IAM audit logs', TRUE, TRUE, FALSE,
        CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 0)
ON CONFLICT (name) DO UPDATE SET resource = 'audit', action = 'read', is_system = TRUE, is_active = TRUE,
    is_deleted = FALSE, deleted_at = NULL, deleted_by = NULL, updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system', version = permissions.version + 1;

UPDATE role_permissions assignment SET revoked_at = CURRENT_TIMESTAMP, revoked_by = 'system',
    reason = 'Expired audit permission assignment replaced', updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system', version = assignment.version + 1
FROM roles role, permissions permission
WHERE assignment.role_id = role.id AND assignment.permission_id = permission.id
    AND role.name = 'ADMIN' AND permission.name = 'audit:read'
    AND assignment.revoked_at IS NULL AND assignment.is_deleted = FALSE AND assignment.expires_at <= CURRENT_TIMESTAMP;

INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by, reason,
                              created_at, created_by, updated_at, updated_by, version)
SELECT role.id, permission.id, CURRENT_TIMESTAMP, 'system', 'Audit log administrator access',
       CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 0
FROM roles role CROSS JOIN permissions permission
WHERE role.name = 'ADMIN' AND permission.name = 'audit:read'
ON CONFLICT DO NOTHING;
