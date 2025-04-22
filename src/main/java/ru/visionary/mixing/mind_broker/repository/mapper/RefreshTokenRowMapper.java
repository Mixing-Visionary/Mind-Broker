package ru.visionary.mixing.mind_broker.repository.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.visionary.mixing.mind_broker.entity.RefreshToken;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class RefreshTokenRowMapper implements RowMapper<RefreshToken> {
    private final UserRowMapper userRowMapper;

    @Override
    public RefreshToken mapRow(ResultSet rs, int rowNum) throws SQLException {
        return RefreshToken.builder()
                .id(rs.getLong("rt_id"))
                .token(rs.getString("token"))
                .expiryDate(rs.getTimestamp("expiry_date").toLocalDateTime())
                .user(userRowMapper.mapRow(rs, rowNum))
                .build();
    }
}
