--liquibase formatted sql

--changeset kim:010-upgrade-users
ALTER TABLE users RENAME COLUMN password TO password_hash;
ALTER TABLE users RENAME COLUMN picture TO picture_url;
ALTER TABLE users RENAME COLUMN is_enabled TO enabled;
ALTER TABLE users RENAME COLUMN is_account_non_expired TO account_non_expired;
ALTER TABLE users RENAME COLUMN is_account_non_locked TO account_non_locked;
ALTER TABLE users RENAME COLUMN is_credentials_non_expired TO credentials_non_expired;

UPDATE users
SET uuid = gen_random_uuid()::text
WHERE uuid IS NULL OR btrim(uuid) = '';

ALTER TABLE users
ALTER COLUMN uuid TYPE UUID USING uuid::uuid,
    ALTER COLUMN uuid SET NOT NULL;

UPDATE users SET email = lower(btrim(email));

UPDATE users
SET phone_number = NULLIF(btrim(phone_number), '');

ALTER TABLE users
ALTER COLUMN username TYPE VARCHAR(50),
    ALTER COLUMN username SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN given_name TYPE VARCHAR(100),
    ALTER COLUMN given_name SET NOT NULL,
    ALTER COLUMN family_name TYPE VARCHAR(100),
    ALTER COLUMN family_name SET NOT NULL,
    ALTER COLUMN gender TYPE VARCHAR(20),
    ALTER COLUMN phone_number TYPE VARCHAR(20),
    ALTER COLUMN picture_url TYPE VARCHAR(500);

ALTER TABLE users
    ADD COLUMN middle_name VARCHAR(100),
    ADD COLUMN nickname VARCHAR(100),
    ADD COLUMN profile_url VARCHAR(500),
    ADD COLUMN website_url VARCHAR(500),
    ADD COLUMN zoneinfo VARCHAR(50),
    ADD COLUMN locale VARCHAR(10) NOT NULL DEFAULT 'en',

    ADD COLUMN address_street_address VARCHAR(255),
    ADD COLUMN address_locality VARCHAR(100),
    ADD COLUMN address_region VARCHAR(100),
    ADD COLUMN address_postal_code VARCHAR(20),
    ADD COLUMN address_country CHAR(2),

    ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN last_login_at TIMESTAMPTZ,
    ADD COLUMN password_changed_at TIMESTAMPTZ,

    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted_at TIMESTAMPTZ,
    ADD COLUMN deleted_by VARCHAR(100),

    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE users
    ADD CONSTRAINT uk_users_uuid UNIQUE (uuid),
    ADD CONSTRAINT uk_users_username UNIQUE (username),
    ADD CONSTRAINT uk_users_email UNIQUE (email),
    ADD CONSTRAINT uk_users_phone_number UNIQUE (phone_number);

ALTER TABLE users
    ADD CONSTRAINT ck_users_failed_login_attempts
        CHECK (failed_login_attempts >= 0),
    ADD CONSTRAINT ck_users_email_lowercase
        CHECK (email = lower(email));

ALTER TABLE users
    ALTER COLUMN created_at DROP DEFAULT,
ALTER COLUMN created_by DROP DEFAULT,
    ALTER COLUMN updated_at DROP DEFAULT,
    ALTER COLUMN updated_by DROP DEFAULT;
