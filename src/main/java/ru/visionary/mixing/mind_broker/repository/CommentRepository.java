package ru.visionary.mixing.mind_broker.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.visionary.mixing.mind_broker.entity.Comment;
import ru.visionary.mixing.mind_broker.repository.mapper.CommentRowMapper;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommentRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CommentRowMapper rowMapper;

    private static final String INSERT_COMMENT = """
        INSERT INTO comment (author, image, comment, created_at)
        VALUES (:author, :image, :comment, current_timestamp)
        """;

    private static final String FIND_BY_IMAGE = """
        SELECT c.*, u.nickname, u.avatar, u.active
        FROM comment c
        JOIN users u ON c.author = u.id
        WHERE c.image = :image
        ORDER BY c.created_at DESC
        LIMIT :size
        OFFSET :size * :page
        """;

    public void save(Comment comment) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("author", comment.author().id())
                .addValue("image", comment.image().id())
                .addValue("comment", comment.comment());

        jdbcTemplate.update(INSERT_COMMENT, params);
    }

    public List<Comment> findByImage(UUID imageUuid, int size, int page) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("image", imageUuid)
                .addValue("size", size)
                .addValue("page", page);

        return jdbcTemplate.query(FIND_BY_IMAGE, params, rowMapper);
    }
}
