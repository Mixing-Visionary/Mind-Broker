package ru.visionary.mixing.mind_broker.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.visionary.mixing.mind_broker.entity.Style;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class StyleRowMapper implements RowMapper<Style> {
    @Override
    public Style mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Style.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .icon(rs.getString("icon"))
                .active(rs.getBoolean("active"))
                .build();
    }
}
