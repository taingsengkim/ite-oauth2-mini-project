--liquibase formatted sql

--changeset kim:024-password-recovery
CREATE TABLE password_reset_tokens (
    token_hash CHAR(64) PRIMARY KEY,
    user_uuid UUID NOT NULL UNIQUE REFERENCES users(uuid) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL CHECK (expires_at > created_at)
);
CREATE INDEX idx_password_reset_expiry ON password_reset_tokens(expires_at);
CREATE TABLE password_reset_limits (
    key_hash CHAR(64) PRIMARY KEY,
    window_start TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    request_count INTEGER NOT NULL CHECK (request_count > 0)
);
CREATE INDEX idx_password_reset_limits_expiry ON password_reset_limits(expires_at);

--changeset kim:024-password-recovery-invalidation splitStatements:false
CREATE FUNCTION invalidate_iam_password_resets() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.password_hash IS DISTINCT FROM NEW.password_hash
       OR OLD.password_changed_at IS DISTINCT FROM NEW.password_changed_at
       OR OLD.email IS DISTINCT FROM NEW.email OR OLD.username IS DISTINCT FROM NEW.username
       OR OLD.enabled IS DISTINCT FROM NEW.enabled OR OLD.is_deleted IS DISTINCT FROM NEW.is_deleted
       OR OLD.account_non_expired IS DISTINCT FROM NEW.account_non_expired
       OR OLD.account_non_locked IS DISTINCT FROM NEW.account_non_locked THEN
        DELETE FROM password_reset_tokens WHERE user_uuid = NEW.uuid;
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER invalidate_iam_password_resets_after_write
AFTER UPDATE ON users FOR EACH ROW EXECUTE FUNCTION invalidate_iam_password_resets();
