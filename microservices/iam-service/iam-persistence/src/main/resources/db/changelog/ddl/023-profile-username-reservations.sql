--liquibase formatted sql

--changeset kim:023-username-reservations
ALTER TABLE users ADD COLUMN profile_picture_managed BOOLEAN NOT NULL DEFAULT FALSE;
CREATE TABLE username_reservations (
    username VARCHAR(50) PRIMARY KEY,
    user_uuid UUID NOT NULL REFERENCES users(uuid)
);
INSERT INTO username_reservations (username, user_uuid) SELECT username, uuid FROM users;

--changeset kim:023-username-reservation-trigger splitStatements:false
CREATE FUNCTION reserve_iam_username() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO username_reservations (username, user_uuid) VALUES (NEW.username, NEW.uuid)
    ON CONFLICT (username) DO NOTHING;
    IF NOT EXISTS (SELECT 1 FROM username_reservations WHERE username = NEW.username AND user_uuid = NEW.uuid) THEN
        RAISE EXCEPTION USING ERRCODE = '23505', CONSTRAINT = 'uk_username_reservations_username',
            MESSAGE = 'Username is reserved by another account';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER reserve_iam_username_after_write
AFTER INSERT OR UPDATE OF username ON users
FOR EACH ROW EXECUTE FUNCTION reserve_iam_username();
