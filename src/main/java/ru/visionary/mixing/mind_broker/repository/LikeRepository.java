package ru.visionary.mixing.mind_broker.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class LikeRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT_LIKE = """
            INSERT INTO likes (user_id, image, like_at)
            VALUES (:userId, :image, current_timestamp)
            """;

    private static final String GET_USER_LIKES_COUNT = """
            SELECT count(*)
            FROM users u
                JOIN image i ON u.id = i.owner
                JOIN likes l ON i.id = l.image
            WHERE u.id = :userId
            """;

    private static final String IS_IMAGE_LIKED = """
            SELECT exists (
                SELECT 1
                FROM likes l
                    JOIN users u ON l.user_id = u.id
                    JOIN image i ON l.image = i.id
                WHERE u.id = :userId
                    AND i.id = :imageUuid
            )
            """;

    private static final String DELETE_LIKE = """
            DELETE FROM likes
            WHERE user_id = :userId AND image = :image
            """;

    public void save(Long userId, UUID imageUuid) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("image", imageUuid);

        try {
            jdbcTemplate.update(INSERT_LIKE, params);
        } catch (DuplicateKeyException e) {
            log.warn("Duplicate like attempt - user {} already liked image {}", userId, imageUuid);
            throw new ServiceException(ErrorCode.ALREADY_LIKED);
        }
    }

    public long getUserLikesCount(long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        return jdbcTemplate.queryForObject(GET_USER_LIKES_COUNT, params, Long.class);
    }

    public boolean isImageLiked(long userId, UUID imageUuid) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("imageUuid", imageUuid);

        return jdbcTemplate.queryForObject(IS_IMAGE_LIKED, params, Boolean.class);
    }

    public int deleteByUserAndImage(Long userId, UUID imageUuid) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("image", imageUuid);

        return jdbcTemplate.update(DELETE_LIKE, params);
    }
}
