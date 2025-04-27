ALTER TABLE IF EXISTS users
    ADD COLUMN IF NOT EXISTS description varchar(255),
    ADD COLUMN IF NOT EXISTS avatar uuid,
    ADD COLUMN IF NOT EXISTS created_at timestamp NOT NULL DEFAULT 'now'::timestamp,
    ADD COLUMN IF NOT EXISTS active boolean NOT NULL DEFAULT true;