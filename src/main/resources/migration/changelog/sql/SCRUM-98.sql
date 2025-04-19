CREATE TABLE IF NOT EXISTS users (
    id bigserial PRIMARY KEY,
    nickname varchar(25) UNIQUE NOT NULL,
    email varchar(255) UNIQUE NOT NULL,
    password varchar(255) NOT NULL,
    admin boolean NOT NULL DEFAULT false
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_nickname ON users(nickname);

CREATE TABLE IF NOT EXISTS refresh_token (
    id bigserial PRIMARY KEY,
    token varchar(255) UNIQUE NOT NULL,
    user_id bigint NOT NULL,
    expiry_date timestamp NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_user ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_expiry ON refresh_token(expiry_date);
