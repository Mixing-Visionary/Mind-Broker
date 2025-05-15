CREATE TABLE IF NOT EXISTS follow (
    follower bigint NOT NULL,
    follow bigint NOT NULL,
    follow_at timestamp NOT NULL,
    PRIMARY KEY (follower, follow),
    FOREIGN KEY (follower) REFERENCES users(id),
    FOREIGN KEY (follow) REFERENCES users(id)
)