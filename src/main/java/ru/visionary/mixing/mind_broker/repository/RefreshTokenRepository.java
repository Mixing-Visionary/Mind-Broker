package ru.visionary.mixing.mind_broker.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.visionary.mixing.mind_broker.entity.RefreshToken;
import ru.visionary.mixing.mind_broker.repository.mapper.RefreshTokenRowMapper;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RefreshTokenRowMapper rowMapper;

    private static final String INSERT_TOKEN = """
        INSERT INTO refresh_token (token, user_id, expiry_date)
        VALUES (:token, :user, :expiryDate)
        RETURNING id
        """;

    private static final String FIND_BY_TOKEN = """
        SELECT u.*, rt.id AS rt_id, rt.token, rt.expiry_date
        FROM refresh_token rt
        JOIN users u ON rt.user_id = u.id
        WHERE token = :token
        """;

    private static final String DELETE_BY_USER = """
        DELETE FROM refresh_token
        WHERE user_id = :user
        """;

    public RefreshToken save(RefreshToken token) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("token", token.getToken())
                .addValue("user", token.getUser().getId())
                .addValue("expiryDate", token.getExpiryDate());

        Long id = jdbcTemplate.queryForObject(INSERT_TOKEN, params, Long.class);
        return token.setId(id);
    }

    public RefreshToken findByToken(String token) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("token", token);

        try {
            return jdbcTemplate.queryForObject(FIND_BY_TOKEN, params, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void deleteByUser(long user) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("user", user);

        jdbcTemplate.update(DELETE_BY_USER, params);
    }
}
