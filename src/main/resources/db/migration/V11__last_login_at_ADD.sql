ALTER TABLE users
    ADD last_login_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now();

COMMENT ON COLUMN users.last_login_at IS '마지막 로그인 일시'