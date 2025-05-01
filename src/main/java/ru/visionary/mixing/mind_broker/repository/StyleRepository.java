package ru.visionary.mixing.mind_broker.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.visionary.mixing.mind_broker.entity.Style;
import ru.visionary.mixing.mind_broker.repository.mapper.StyleRowMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StyleRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final StyleRowMapper rowMapper;

    private static final String INSERT_STYLE = """
            INSERT INTO style (name)
            VALUES (:name)
            """;

    private static final String FIND_BY_STRING = """
            SELECT *
            FROM style
            WHERE name = :name
            """;

    private static final String FIND_ALL = """
            SELECT *
            FROM style
            """;

    private static final String FIND_ALL_ACTIVE = """
            SELECT *
            FROM style
            WHERE active = true
            """;

    private static final String UPDATE_ACTIVE = """
            UPDATE style
            SET active = :active
            WHERE id IN (:ids)
            """;

    private static final String UPDATE_ICON = """
            UPDATE style
            SET icon = :icon
            WHERE id = :id
            """;

    public void save(Style style) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", style.name());

        jdbcTemplate.update(INSERT_STYLE, params);
    }

    public void save(List<String> styles) {
        MapSqlParameterSource[] params = styles.stream()
                .map(style -> new MapSqlParameterSource().addValue("name", style))
                .toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(INSERT_STYLE, params);
    }

    public Style findByName(String name) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name);

        try {
            return jdbcTemplate.queryForObject(FIND_BY_STRING, params, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Style> findAll() {
        return jdbcTemplate.query(FIND_ALL, rowMapper);
    }

    public List<Style> findAllActive() {
        return jdbcTemplate.query(FIND_ALL_ACTIVE, rowMapper);
    }

    public void updateActive(List<Integer> ids, boolean active) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", ids)
                .addValue("active", active);

        jdbcTemplate.update(UPDATE_ACTIVE, params);
    }

    public void updateIcon(int id, String icon) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("icon", icon);

        jdbcTemplate.update(UPDATE_ICON, params);
    }
}
