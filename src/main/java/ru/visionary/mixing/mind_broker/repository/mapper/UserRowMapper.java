package ru.visionary.mixing.mind_broker.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.visionary.mixing.mind_broker.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .nickname(rs.getString("nickname"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .avatar(rs.getObject("avatar", UUID.class))
                .description(rs.getString("description"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .admin(rs.getBoolean("admin"))
                .active(rs.getBoolean("active"))
                .build();
    }
}
