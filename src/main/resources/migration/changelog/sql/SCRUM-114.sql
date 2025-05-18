CREATE TABLE IF NOT EXISTS likes (
    user_id bigint NOT NULL,
    image uuid NOT NULL,
    like_at timestamp NOT NULL,
    PRIMARY KEY (user_id, image),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (image) REFERENCES image(id)
);

CREATE TABLE IF NOT EXISTS comment (
    id bigserial PRIMARY KEY,
    author bigint NOT NULL,
    image uuid NOT NULL,
    comment varchar(255) NOT NULL,
    created_at timestamp NOT NULL,
    FOREIGN KEY (author) REFERENCES users(id),
    FOREIGN KEY (image) REFERENCES image(id)
);

