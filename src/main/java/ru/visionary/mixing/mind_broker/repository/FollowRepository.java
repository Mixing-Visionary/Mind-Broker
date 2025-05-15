package ru.visionary.mixing.mind_broker.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.visionary.mixing.mind_broker.entity.Follow;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;

@Repository
@RequiredArgsConstructor
public class FollowRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT_FOLLOW = """
            INSERT INTO follow (follower, follow, follow_at)
            VALUES (:follower, :follow, current_timestamp)
            """;

    private static final String DELETE_FOLLOW = """
            DELETE FROM follow
            WHERE follower = :follower AND follow = :follow
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
}
