package ru.visionary.mixing.mind_broker.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.visionary.mixing.mind_broker.entity.Processing;
import ru.visionary.mixing.mind_broker.entity.ProcessingStatus;
import ru.visionary.mixing.mind_broker.entity.Style;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class ProcessingRowMapper implements RowMapper<Processing> {
    @Override
    public Processing mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Processing.builder()
                .id(rs.getObject("id", UUID.class))
                .style(Style.builder()
                        .id(rs.getInt("style_id"))
                        .name(rs.getString("style_name"))
                        .icon(rs.getString("icon"))
                        .active(rs.getBoolean("style_active"))
                        .build())
                .startTime(rs.getTimestamp("start_time").toLocalDateTime())
                .status(ProcessingStatus.valueOf(rs.getString("status")))
                .statusAt(rs.getTimestamp("status_at").toLocalDateTime())
                .build();
    }
}
