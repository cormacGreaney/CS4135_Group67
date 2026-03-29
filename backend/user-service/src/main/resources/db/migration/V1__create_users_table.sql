CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
);
