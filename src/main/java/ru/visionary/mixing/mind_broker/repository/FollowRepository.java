package ru.visionary.mixing.mind_broker.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.visionary.mixing.mind_broker.entity.Follow;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.mapper.UserRowMapper;

import java.util.List;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class FollowRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    private static final String INSERT_FOLLOW = """
            INSERT INTO follow (follower, follow, follow_at)
            VALUES (:follower, :follow, current_timestamp)
            """;

    private static final String DELETE_FOLLOW = """
            DELETE FROM follow
            WHERE follower = :follower AND follow = :follow
            """;

    private static final String GET_FOLLOWS_BY_USER = """
            SELECT u.*
            FROM follow f
            JOIN users u ON f.follow = u.id
            WHERE f.follower = :user
            LIMIT :size
            OFFSET :size * :page
            """;

    private static final String GET_FOLLOWERS_BY_USER = """
            SELECT u.*
            FROM follow f
            JOIN users u ON f.follower = u.id
            WHERE f.follow = :user
            LIMIT :size
            OFFSET :size * :page
            """;

    public void save(Follow follow) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("follower", follow.follower().id())
                .addValue("follow", follow.follow().id());

        try {
            jdbcTemplate.update(INSERT_FOLLOW, params);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ErrorCode.ALREADY_FOLLOWING);
        }
    }

    public int deleteByFollowerAndFollow(Long followerId, Long followId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("follower", followerId)
                .addValue("follow", followId);

        return jdbcTemplate.update(DELETE_FOLLOW, params);
    }

    public List<User> getFollows(Long userId, int size, int page) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("user", userId)
                .addValue("size", size)
                .addValue("page", page);

        try (Stream<User> stream = jdbcTemplate.queryForStream(GET_FOLLOWS_BY_USER, params, userRowMapper)) {
            return stream.toList();
        }
    }

    public List<User> getFollowers(Long userId, int size, int page) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("user", userId)
                .addValue("size", size)
                .addValue("page", page);

        try (Stream<User> stream = jdbcTemplate.queryForStream(GET_FOLLOWERS_BY_USER, params, userRowMapper)) {
            return stream.toList();
        }
    }
}
