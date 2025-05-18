package ru.visionary.mixing.mind_broker.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.visionary.mixing.mind_broker.entity.Processing;
import ru.visionary.mixing.mind_broker.entity.ProcessingStatus;
import ru.visionary.mixing.mind_broker.repository.mapper.ProcessingRowMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProcessingRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ProcessingRowMapper rowMapper;

    private static final String INSERT_PROCESSING = """
            INSERT INTO processing (id, user_id, style, start_time, status, status_at)
            VALUES (:id, :userId, :style, current_timestamp, 'PENDING'::processing_status, current_timestamp)
            """;

    private static final String FIND_BY_ID = """
            SELECT p.*, s.id AS style_id, s.name AS style_name, s.icon, s.active AS style_active
            FROM processing p
            JOIN style s ON p.style = s.id
            WHERE p.id = :id
            """;

    private static final String GET_START_TIME_BY_ID = """
            SELECT start_time
            FROM processing
            WHERE id = :id
            """;

    private static final String UPDATE_STATUS = """
            UPDATE processing
            SET status = :status::processing_status, status_at = current_timestamp
            WHERE id = :id
            """;

    private static final String CANCEL_PENDING_BY_ID = """
            UPDATE processing
            SET status = 'CANCELED'::processing_status, status_at = current_timestamp
            WHERE id = :id
                AND status = 'PENDING'::processing_status
            """;

    private static final String CLEAR_RESULT_WHERE_LOADED_BEFORE = """
            UPDATE processing
            SET result = null
            WHERE status = 'COMPLETED'::processing_status
                AND result IS NOT NULL
                AND status_at < :time
            """;

    private static final String CANCEL_LONG_PROCESSING = """
            WITH updated_processing AS (
                UPDATE processing
                SET status = 'CANCELED'::processing_status, status_at = current_timestamp
                WHERE status = 'PROCESSING'::processing_status
                    AND status_at < :time
                RETURNING *
            )
            SELECT p.*, s.id AS style_id, s.name AS style_name, s.icon, s.active AS style_active
            FROM updated_processing p
            JOIN style s ON p.style = s.id
            """;

    public void save(Processing processing) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", processing.id())
                .addValue("userId", processing.user().id())
                .addValue("style", processing.style().id());

        jdbcTemplate.update(INSERT_PROCESSING, params);
    }

    public Processing findById(UUID id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        try {
            return jdbcTemplate.queryForObject(FIND_BY_ID, params, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public LocalDateTime getStartTimeById(UUID id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return jdbcTemplate.queryForObject(GET_START_TIME_BY_ID, params, Timestamp.class).toLocalDateTime();
    }

    public void updateStatus(UUID id, ProcessingStatus status) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status.name());
        jdbcTemplate.update(UPDATE_STATUS, params);
    }

    public int cancelPending(UUID id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return jdbcTemplate.update(CANCEL_PENDING_BY_ID, params);
    }

    public void clearResultWhereLoadedBefore(LocalDateTime time) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("time", time);

        jdbcTemplate.update(CLEAR_RESULT_WHERE_LOADED_BEFORE, params);
    }

    public List<Processing> cancelLongProcessing(LocalDateTime time) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("time", time);

        return jdbcTemplate.queryForStream(CANCEL_LONG_PROCESSING, params, rowMapper).toList();
    }
}
