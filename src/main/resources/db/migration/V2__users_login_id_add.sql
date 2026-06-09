ALTER TABLE users
    ADD login_id VARCHAR(30);

ALTER TABLE users
    ADD CONSTRAINT uc_users_login UNIQUE (login_id);