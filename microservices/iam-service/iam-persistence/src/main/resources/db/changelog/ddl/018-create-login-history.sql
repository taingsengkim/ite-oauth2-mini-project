--liquibase formatted sql

--changeset kim:018-create-login-history
CREATE TABLE user_login_history (
    uuid UUID PRIMARY KEY,
    user_uuid UUID,
    attempted_login VARCHAR(255),
    provider VARCHAR(100) NOT NULL,
    outcome VARCHAR(10) NOT NULL CHECK (outcome IN ('SUCCESS', 'FAILURE')),
    reason VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    ip_address VARCHAR(64),
    user_agent VARCHAR(512)
);
-- History deliberately survives account deletion and also records unknown accounts.
CREATE INDEX idx_login_history_time ON user_login_history (occurred_at DESC, uuid DESC);
CREATE INDEX idx_login_history_user ON user_login_history (user_uuid, occurred_at DESC);
CREATE INDEX idx_login_history_login ON user_login_history (lower(attempted_login), occurred_at DESC);
CREATE INDEX idx_login_history_failed_ip ON user_login_history (ip_address, occurred_at DESC) WHERE outcome = 'FAILURE';
