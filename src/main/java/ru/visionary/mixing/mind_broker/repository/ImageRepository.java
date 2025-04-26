package ru.visionary.mixing.mind_broker.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.visionary.mixing.mind_broker.entity.Image;

import java.sql.Timestamp;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ImageRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT_IMAGE = """
            INSERT INTO image (id, owner, protection, created_at)
            VALUES (:id, :owner, :protection::protection, :createdAt)
            """;

    public UUID save(Image image) {
        UUID uuid = UUID.randomUUID();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", uuid)
                .addValue("owner", image.getOwner().getId())
                .addValue("protection", image.getProtection().toString().toLowerCase())
                .addValue("createdAt", Timestamp.valueOf(image.getCreatedAt()));

        jdbcTemplate.update(INSERT_IMAGE, params);
        return uuid;
    }
}
