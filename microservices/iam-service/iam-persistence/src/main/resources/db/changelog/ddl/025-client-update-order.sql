--liquibase formatted sql

--changeset kim:025-client-update-order
ALTER TABLE oauth2_registered_client ADD COLUMN updated_at TIMESTAMPTZ;
UPDATE oauth2_registered_client SET updated_at = client_id_issued_at;
ALTER TABLE oauth2_registered_client ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE oauth2_registered_client ALTER COLUMN updated_at SET NOT NULL;
CREATE INDEX idx_registered_client_updated ON oauth2_registered_client(updated_at DESC, id);

--changeset kim:025-client-update-trigger splitStatements:false
CREATE FUNCTION track_registered_client_update() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW IS DISTINCT FROM OLD THEN
        NEW.updated_at = clock_timestamp();
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER track_registered_client_update_before_write
BEFORE UPDATE ON oauth2_registered_client
FOR EACH ROW EXECUTE FUNCTION track_registered_client_update();
