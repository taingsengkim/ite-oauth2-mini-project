--liquibase formatted sql

--changeset kim:006-upgrade-permissions
ALTER TABLE permissions RENAME COLUMN permission TO name;
UPDATE permissions SET name = 'legacy:' || id WHERE name IS NULL OR btrim(name) = '';
UPDATE permissions SET name = name || ':access' WHERE position(':' in name) = 0;
ALTER TABLE permissions ALTER COLUMN name TYPE VARCHAR(100);
ALTER TABLE permissions ALTER COLUMN name SET NOT NULL;
ALTER TABLE permissions ADD CONSTRAINT uk_permissions_name UNIQUE (name);

ALTER TABLE permissions
    ADD COLUMN uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    ADD COLUMN resource VARCHAR(50),
    ADD COLUMN action VARCHAR(50),
    ADD COLUMN is_system BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted_at TIMESTAMPTZ,
    ADD COLUMN deleted_by VARCHAR(100),
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

UPDATE permissions
SET resource = split_part(name, ':', 1),
    action = substring(name from position(':' in name) + 1);

ALTER TABLE permissions ALTER COLUMN resource SET NOT NULL;
ALTER TABLE permissions ALTER COLUMN action SET NOT NULL;
ALTER TABLE permissions ADD CONSTRAINT uk_permissions_uuid UNIQUE (uuid);

-- Future inserts use the domain UUID and Spring Data auditing.
ALTER TABLE permissions ALTER COLUMN uuid DROP DEFAULT;
ALTER TABLE permissions ALTER COLUMN created_at DROP DEFAULT;
ALTER TABLE permissions ALTER COLUMN created_by DROP DEFAULT;
ALTER TABLE permissions ALTER COLUMN updated_at DROP DEFAULT;
ALTER TABLE permissions ALTER COLUMN updated_by DROP DEFAULT;
