CREATE TYPE protection AS ENUM ('public', 'private');

CREATE TABLE IF NOT EXISTS image (
    id uuid PRIMARY KEY,
    owner bigint NOT NULL,
    protection protection NOT NULL,
    created_at timestamp NOT NULL,
    FOREIGN KEY (owner) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_image_owner ON image(owner);