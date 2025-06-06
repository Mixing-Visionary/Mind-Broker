package ru.visionary.mixing.mind_broker.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.repository.mapper.ImageRowMapper;

import java.util.List;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class FeedRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ImageRowMapper imageRowMapper;

    private static final String FEED_BY_NEW = """
            SELECT i.*, u.nickname, u.avatar, u.active
            FROM image i
                JOIN public.users u on i.owner = u.id
            WHERE protection = 'public'::protection
                AND (:currentUser::bigint IS NULL OR u.id != :currentUser)
            ORDER BY created_at DESC
            LIMIT :size
            OFFSET :size * :page
            """;

    private static final String FEED_BY_POPULAR = """
            SELECT i.*, u.nickname, u.avatar, u.active
            FROM image i
                JOIN public.users u on i.owner = u.id
                LEFT JOIN likes l on i.id = l.image
            WHERE i.protection = 'public'::protection
                AND (:currentUser::bigint IS NULL OR u.id != :currentUser)
            GROUP BY i.id, i.created_at, u.id
            ORDER BY count(l.*) DESC, i.created_at DESC
            LIMIT :size
            OFFSET :size * :page
            """;

    private static final String FEED_BY_FOLLOW = """
            SELECT i.*, u.nickname, u.avatar, u.active
            FROM follow f
                JOIN users u on f.follow = u.id
                JOIN image i on u.id = i.owner
            WHERE f.follower = :currentUser
                AND i.protection = 'public'::protection
            ORDER BY created_at DESC
            LIMIT :size
            OFFSET :size * :page
            """;

    public List<Image> getFeedByNew(Long currentUserId, int size, int page) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("currentUser", currentUserId)
                .addValue("size", size)
                .addValue("page", page);

        try (Stream<Image> stream = jdbcTemplate.queryForStream(FEED_BY_NEW, params, imageRowMapper)) {
            return stream.toList();
        }
    }

    public List<Image> getFeedByPopular(Long currentUserId, int size, int page) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("currentUser", currentUserId)
                .addValue("size", size)
                .addValue("page", page);

        try (Stream<Image> stream = jdbcTemplate.queryForStream(FEED_BY_POPULAR, params, imageRowMapper)) {
            return stream.toList();
        }
    }

    public List<Image> getFeedByFollow(long userId, int size, int page) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("currentUser", userId)
                .addValue("size", size)
                .addValue("page", page);

        try (Stream<Image> stream = jdbcTemplate.queryForStream(FEED_BY_FOLLOW, params, imageRowMapper)) {
            return stream.toList();
        }
    }
}
