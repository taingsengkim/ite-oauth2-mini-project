--liquibase formatted sql

--changeset kim:014-bootstrap-admin-assignment
INSERT INTO user_roles (
    user_id, role_id,
    assigned_at, assigned_by, reason,
    created_at, created_by, updated_at, updated_by, version
)
SELECT
    u.id,
    r.id,
    CURRENT_TIMESTAMP,
    'system',
    'Initial administrator assignment',
    CURRENT_TIMESTAMP, 'system',
    CURRENT_TIMESTAMP, 'system', 0
FROM users u
         CROSS JOIN roles r
WHERE u.uuid = 'd563fdee-a5e2-49a3-8395-b093243e31c8'
  AND u.is_deleted = FALSE
  AND r.name = 'ADMIN'
  AND r.is_active = TRUE
  AND r.is_deleted = FALSE
    ON CONFLICT DO NOTHING;