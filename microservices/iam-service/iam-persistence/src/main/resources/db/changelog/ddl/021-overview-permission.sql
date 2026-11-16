--liquibase formatted sql

--changeset kim:021-overview-permission
INSERT INTO permissions (uuid, name, resource, action, description, is_system, is_active, is_deleted,
                         created_at, created_by, updated_at, updated_by, version)
VALUES (gen_random_uuid(), 'overview:read', 'overview', 'read', 'Read IAM administrator overview', TRUE, TRUE, FALSE,
        CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 0)
ON CONFLICT (name) DO UPDATE SET resource = 'overview', action = 'read', is_system = TRUE, is_active = TRUE,
    is_deleted = FALSE, deleted_at = NULL, deleted_by = NULL, updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system', version = permissions.version + 1;

UPDATE role_permissions assignment SET revoked_at = CURRENT_TIMESTAMP, revoked_by = 'system',
    reason = 'Expired overview permission assignment replaced', updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system', version = assignment.version + 1
FROM roles role, permissions permission
WHERE assignment.role_id = role.id AND assignment.permission_id = permission.id
    AND role.name = 'ADMIN' AND permission.name = 'overview:read'
    AND assignment.revoked_at IS NULL AND assignment.is_deleted = FALSE AND assignment.expires_at <= CURRENT_TIMESTAMP;

INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by, reason,
                              created_at, created_by, updated_at, updated_by, version)
SELECT role.id, permission.id, CURRENT_TIMESTAMP, 'system', 'Overview administrator access',
       CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 0
FROM roles role CROSS JOIN permissions permission
WHERE role.name = 'ADMIN' AND permission.name = 'overview:read'
ON CONFLICT DO NOTHING;
