package ru.visionary.mixing.mind_broker.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.repository.mapper.UserRowMapper;

import java.util.UUID;

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

    private static final String FIND_BY_ID = """
            SELECT *
            FROM users
            WHERE id = :id
            """;

    private static final String FIND_BY_EMAIL = """
            SELECT *
            FROM users
            WHERE
                lower(email) = lower(:email)
            """;

    private static final String FIND_BY_EMAIL_OR_NICKNAME = """
            SELECT *
            FROM users
            WHERE lower(email) = lower(:email)
                OR lower(nickname) = lower(:nickname)
            LIMIT 1
            """;

    private static final String UPDATE_USER = """
            UPDATE users
            SET nickname = COALESCE(:nickname, nickname),
                description = COALESCE(:description, description),
                password = COALESCE(:password, password),
                avatar = COALESCE(:avatar, avatar)
            WHERE id = :id
            """;

    private static final String DELETE_USER = """
            UPDATE users
            SET active = false
            WHERE id = :id
            """;

    private static final String DELETE_AVATAR = """
            UPDATE users
            SET avatar = null
            WHERE id = :id
            """;

    public Long save(User user) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nickname", user.nickname())
                .addValue("email", user.email())
                .addValue("password", user.getPassword());

        return jdbcTemplate.queryForObject(INSERT_USER, params, Long.class);
    }

    public User findById(long id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        try {
            return jdbcTemplate.queryForObject(FIND_BY_ID, params, userRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User findByEmail(String email) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email);

        try {
            return jdbcTemplate.queryForObject(FIND_BY_EMAIL, params, userRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User findByEmailOrNickname(String email, String nickname) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("nickname", nickname);

        try {
            return jdbcTemplate.queryForObject(FIND_BY_EMAIL_OR_NICKNAME, params, userRowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void updateUser(long userId, String nickname, String description, String password, UUID avatar) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", userId)
                .addValue("nickname", nickname)
                .addValue("description", description)
                .addValue("password", password)
                .addValue("avatar", avatar);

        jdbcTemplate.update(UPDATE_USER, params);
    }

    public void deleteUser(long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", userId);

        jdbcTemplate.update(DELETE_USER, params);
    }

    public void deleteAvatar(long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", userId);

        jdbcTemplate.update(DELETE_AVATAR, params);
    }
}
