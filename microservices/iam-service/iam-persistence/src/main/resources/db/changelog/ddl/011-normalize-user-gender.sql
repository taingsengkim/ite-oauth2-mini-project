--liquibase formatted sql

--changeset kim:011-normalize-user-gender
UPDATE users
SET gender = NULLIF(lower(btrim(gender)), '')
WHERE gender IS NOT NULL;

ALTER TABLE users ADD CONSTRAINT ck_users_gender
    CHECK (gender IS NULL OR gender IN ('female', 'male', 'other'));