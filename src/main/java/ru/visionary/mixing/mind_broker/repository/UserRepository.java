package ru.visionary.mixing.mind_broker.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.repository.mapper.UserRowMapper;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    private static final String INSERT_USER = """
            INSERT INTO users (nickname, email, password)
            VALUES (:nickname, :email, :password)
            RETURNING id
            """;

    private static final String FIND_BY_EMAIL = """
            SELECT *
            FROM users
            WHERE
                email = :email
            """;

    private static final String FIND_BY_EMAIL_OR_NICKNAME = """
            SELECT *
            FROM users
            WHERE email = :email
                OR nickname = :nickname
            """;

    public User save(User user) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nickname", user.getNickname().toLowerCase())
                .addValue("email", user.getEmail().toLowerCase())
                .addValue("password", user.getPassword());

        Long id = jdbcTemplate.queryForObject(INSERT_USER, params, Long.class);
        return user.setId(id);
    }

    public User findByEmail(String email) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email.toLowerCase());

        try {
            return jdbcTemplate.queryForObject(FIND_BY_EMAIL, params, userRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User findByEmailOrNickname(String email, String nickname) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email.toLowerCase())
                .addValue("nickname", nickname.toLowerCase());

        try {
            return jdbcTemplate.queryForObject(FIND_BY_EMAIL_OR_NICKNAME, params, userRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
