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
        return new Image()
                .setId(rs.getObject("id", UUID.class))
                .setProtection(Protection.valueOf(rs.getString("protection").toUpperCase()))
                .setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime())
                .setOwner(new User()
                        .setId(rs.getLong("owner"))
                        .setNickname(rs.getString("nickname"))
                );
    }
}
