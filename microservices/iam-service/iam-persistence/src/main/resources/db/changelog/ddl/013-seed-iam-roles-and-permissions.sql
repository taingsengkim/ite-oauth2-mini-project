--liquibase formatted sql

--changeset kim:013-seed-iam-roles-and-permissions
WITH definitions(resource, action) AS (
    VALUES
        ('user', 'create'),
        ('user', 'read'),
        ('user', 'update'),
        ('user', 'delete'),
        ('user', 'assign'),
        ('role', 'create'),
        ('role', 'read'),
        ('role', 'update'),
        ('role', 'delete'),
        ('role', 'assign'),
        ('permission', 'create'),
        ('permission', 'read'),
        ('permission', 'update'),
        ('permission', 'delete')
)
INSERT INTO permissions (
    uuid, name, resource, action, description,
    is_system, is_active, is_deleted,
    created_at, created_by, updated_at, updated_by, version
)
SELECT
    gen_random_uuid(),
    resource || ':' || action,
    resource,
    action,
    'IAM ' || resource || ' ' || action,
    TRUE, TRUE, FALSE,
    CURRENT_TIMESTAMP, 'system',
    CURRENT_TIMESTAMP, 'system', 0
FROM definitions
ON CONFLICT (name) DO UPDATE SET
    resource = EXCLUDED.resource,
                          action = EXCLUDED.action,
                          is_system = TRUE,
                          is_active = TRUE,
                          is_deleted = FALSE,
                          deleted_at = NULL,
                          deleted_by = NULL,
                          updated_at = CURRENT_TIMESTAMP,
                          updated_by = 'system',
                          version = permissions.version + 1;

INSERT INTO roles (
    uuid, name, description,
    is_system, is_active, is_deleted,
    created_at, created_by, updated_at, updated_by, version
)
VALUES
    (
        gen_random_uuid(), 'ADMIN', 'IAM administrator',
        TRUE, TRUE, FALSE,
        CURRENT_TIMESTAMP, 'system',
        CURRENT_TIMESTAMP, 'system', 0
    ),
    (
        gen_random_uuid(), 'USER', 'Standard user',
        TRUE, TRUE, FALSE,
        CURRENT_TIMESTAMP, 'system',
        CURRENT_TIMESTAMP, 'system', 0
    )
    ON CONFLICT (name) DO UPDATE SET
    is_system = TRUE,
                              is_active = TRUE,
                              is_deleted = FALSE,
                              deleted_at = NULL,
                              deleted_by = NULL,
                              updated_at = CURRENT_TIMESTAMP,
                              updated_by = 'system',
                              version = roles.version + 1;

UPDATE role_permissions assignment
SET revoked_at = CURRENT_TIMESTAMP,
    revoked_by = 'system',
    reason = 'Expired bootstrap assignment replaced',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system',
    version = assignment.version + 1
    FROM roles role
WHERE assignment.role_id = role.id
  AND role.name = 'ADMIN'
  AND assignment.revoked_at IS NULL
  AND assignment.is_deleted = FALSE
  AND assignment.expires_at <= CURRENT_TIMESTAMP;

INSERT INTO role_permissions (
    role_id, permission_id,
    assigned_at, assigned_by, reason,
    created_at, created_by, updated_at, updated_by, version
)
SELECT
    role.id,
    permission.id,
    CURRENT_TIMESTAMP,
    'system',
    'Initial administrator permissions',
    CURRENT_TIMESTAMP, 'system',
    CURRENT_TIMESTAMP, 'system', 0
FROM roles role
         CROSS JOIN permissions permission
WHERE role.name = 'ADMIN'
  AND permission.is_system = TRUE
  AND permission.is_active = TRUE
  AND permission.is_deleted = FALSE
  AND permission.resource IN ('user', 'role', 'permission')
    ON CONFLICT DO NOTHING;
