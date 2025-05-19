package ru.visionary.mixing.mind_broker.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.repository.mapper.ImageRowMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class ImageRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ImageRowMapper rowMapper;

    private static final String INSERT_IMAGE = """
            INSERT INTO image (id, owner, protection, created_at)
            VALUES (:id, :owner, :protection::protection, current_timestamp)
            """;

    private static final String FIND_BY_ID = """
            SELECT i.*, u.nickname, u.avatar, u.active
            FROM image i
            JOIN users u ON i.owner = u.id
            WHERE i.id = :id
            """;

    private static final String FIND_BY_OWNER_AND_PROTECTION = """
            SELECT i.*, u.nickname, u.avatar, u.active
            FROM image i
            JOIN users u ON i.owner = u.id
            WHERE i.owner = :ownerId
                AND i.protection = :protection::protection
            ORDER BY i.created_at DESC
            LIMIT :size
            OFFSET :size * :page
            """;

    private static final String UPDATE_PROTECTION = """
            UPDATE image
            SET protection = :protection::protection
            WHERE id = :id
            """;

    private static final String DELETE_BY_ID = """
            DELETE FROM image
            WHERE id = :id
            """;

    public UUID save(Image image) {
        UUID uuid = UUID.randomUUID();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", uuid)
                .addValue("owner", image.owner().id())
                .addValue("protection", image.protection().toString().toLowerCase());

        jdbcTemplate.update(INSERT_IMAGE, params);
        return uuid;
    }

    public Image findById(UUID id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        try {
            return jdbcTemplate.queryForObject(FIND_BY_ID, params, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Image> findByOwnerAndProtection(long ownerId, Protection protection, int size, int page) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ownerId", ownerId)
                .addValue("protection", protection.toString().toLowerCase())
                .addValue("size", size)
                .addValue("page", page);

        try (Stream<Image> stream = jdbcTemplate.queryForStream(FIND_BY_OWNER_AND_PROTECTION, params, rowMapper)) {
            return stream.toList();
        }
    }

    public void updateProtection(UUID id, Protection protection) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("protection", protection.toString().toLowerCase());

        jdbcTemplate.update(UPDATE_PROTECTION, params);
    }

    public void deleteById(UUID id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        jdbcTemplate.update(DELETE_BY_ID, params);
    }
}
