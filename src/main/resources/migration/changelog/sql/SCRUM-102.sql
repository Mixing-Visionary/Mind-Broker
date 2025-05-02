CREATE TYPE processing_status AS ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'CANCELED', 'FAILED');

CREATE TABLE IF NOT EXISTS style (
    id serial PRIMARY KEY,
    name varchar(255) NOT NULL,
    icon varchar(255),
    active boolean NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX idx_style_name ON style(name);

CREATE TABLE IF NOT EXISTS processing (
    id uuid PRIMARY KEY,
    user_id bigint NOT NULL,
    style int NOT NULL,
    start_time timestamp NOT NULL,
    status processing_status NOT NULL,
    status_at timestamp NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (style) REFERENCES style(id)
);

DROP INDEX idx_users_email;
DROP INDEX idx_users_nickname;

CREATE UNIQUE INDEX idx_users_lower_email ON users(lower(email));
CREATE UNIQUE INDEX idx_users_lower_nickname ON users(lower(nickname));