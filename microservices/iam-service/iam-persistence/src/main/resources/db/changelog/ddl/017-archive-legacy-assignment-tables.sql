--liquibase formatted sql

--changeset kim:017-archive-legacy-assignment-tables
-- Migrations 007, 012 and 015 copied these relationships into the audited
-- assignment tables, but left the original join tables in the public schema.
-- Keep the old data available for reference without exposing duplicate tables
-- alongside the tables used by the current JPA entities.
CREATE SCHEMA IF NOT EXISTS legacy_iam;

ALTER TABLE users_roles SET SCHEMA legacy_iam;
ALTER TABLE users_permissions SET SCHEMA legacy_iam;
ALTER TABLE roles_permissions SET SCHEMA legacy_iam;

--rollback ALTER TABLE legacy_iam.roles_permissions SET SCHEMA public;
--rollback ALTER TABLE legacy_iam.users_permissions SET SCHEMA public;
--rollback ALTER TABLE legacy_iam.users_roles SET SCHEMA public;
