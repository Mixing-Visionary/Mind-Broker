package ru.visionary.mixing.mind_broker.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.visionary.mixing.mind_broker.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .nickname(rs.getString("nickname"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .admin(rs.getBoolean("admin"))
                .build();
    }
}
