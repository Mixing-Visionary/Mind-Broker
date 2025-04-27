package ru.visionary.mixing.mind_broker.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class ImageRowMapper implements RowMapper<Image> {
    @Override
    public Image mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Image.builder()
                .id(rs.getObject("id", UUID.class))
                .protection(Protection.valueOf(rs.getString("protection").toUpperCase()))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .owner(User.builder()
                        .id(rs.getLong("owner"))
                        .nickname(rs.getString("nickname"))
                        .avatar(rs.getObject("avatar", UUID.class))
                        .active(rs.getBoolean("active"))
                        .build()
                )
                .build();
    }
}
