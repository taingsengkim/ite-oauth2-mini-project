--liquibase formatted sql

--changeset kim:009-rename-role-permissions
ALTER TABLE role_perms RENAME TO role_permissions;
ALTER TABLE role_permissions RENAME CONSTRAINT role_perms_pkey TO role_permissions_pkey;
ALTER TABLE role_permissions RENAME CONSTRAINT role_perms_role_id_fkey TO role_permissions_role_id_fkey;
ALTER TABLE role_permissions RENAME CONSTRAINT role_perms_permission_id_fkey TO role_permissions_permission_id_fkey;
ALTER TABLE role_permissions RENAME CONSTRAINT ck_role_perms_expiry TO ck_role_permissions_expiry;
ALTER TABLE role_permissions RENAME CONSTRAINT ck_role_perms_revocation TO ck_role_permissions_revocation;
ALTER INDEX uk_role_perms_unrevoked RENAME TO uk_role_permissions_unrevoked;
ALTER INDEX ix_role_perms_permission RENAME TO ix_role_permissions_permission;
ALTER SEQUENCE role_perms_id_seq RENAME TO role_permissions_id_seq;
