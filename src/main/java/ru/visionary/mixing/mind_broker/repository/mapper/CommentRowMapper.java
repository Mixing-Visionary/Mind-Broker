package ru.visionary.mixing.mind_broker.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.visionary.mixing.mind_broker.entity.Comment;
import ru.visionary.mixing.mind_broker.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class CommentRowMapper implements RowMapper<Comment> {
    @Override
    public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Comment.builder()
                .id(rs.getLong("id"))
                .author(User.builder()
                        .id(rs.getLong("author"))
                        .nickname(rs.getString("nickname"))
                        .avatar(rs.getObject("avatar", UUID.class))
                        .active(rs.getBoolean("active"))
                        .build())
                .comment(rs.getString("comment"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
