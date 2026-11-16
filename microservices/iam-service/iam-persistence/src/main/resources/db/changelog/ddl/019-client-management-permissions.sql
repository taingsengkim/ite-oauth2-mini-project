--liquibase formatted sql

--changeset kim:019-client-management-permissions
CREATE UNIQUE INDEX uk_oauth2_registered_client_client_id ON oauth2_registered_client (client_id);

INSERT INTO permissions (uuid, name, resource, action, description, is_system, is_active, is_deleted,
                         created_at, created_by, updated_at, updated_by, version)
SELECT gen_random_uuid(), 'client:' || action, 'client', action, 'IAM client ' || action,
       TRUE, TRUE, FALSE, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 0
FROM (VALUES ('create'), ('read'), ('update'), ('delete')) AS definitions(action)
ON CONFLICT (name) DO UPDATE SET resource = 'client', action = EXCLUDED.action, is_system = TRUE,
    is_active = TRUE, is_deleted = FALSE, deleted_at = NULL, deleted_by = NULL,
    updated_at = CURRENT_TIMESTAMP, updated_by = 'system', version = permissions.version + 1;

UPDATE role_permissions assignment SET revoked_at = CURRENT_TIMESTAMP, revoked_by = 'system',
    reason = 'Expired client permission assignment replaced', updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system', version = assignment.version + 1
FROM roles role, permissions permission
WHERE assignment.role_id = role.id AND assignment.permission_id = permission.id
    AND role.name = 'ADMIN' AND permission.resource = 'client'
    AND assignment.revoked_at IS NULL AND assignment.is_deleted = FALSE AND assignment.expires_at <= CURRENT_TIMESTAMP;

INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by, reason,
                              created_at, created_by, updated_at, updated_by, version)
SELECT role.id, permission.id, CURRENT_TIMESTAMP, 'system', 'Client management administrator permissions',
       CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 0
FROM roles role CROSS JOIN permissions permission
WHERE role.name = 'ADMIN' AND permission.resource = 'client' AND permission.is_active = TRUE AND permission.is_deleted = FALSE
ON CONFLICT DO NOTHING;
