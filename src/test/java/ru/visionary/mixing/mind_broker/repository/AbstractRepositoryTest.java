package ru.visionary.mixing.mind_broker.repository;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@Testcontainers
@Slf4j
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public abstract class AbstractRepositoryTest {
    @Autowired
    protected NamedParameterJdbcTemplate jdbcTemplate;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.liquibase.enabled", () -> true);
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        postgres.start();
        applyMigrations();
    }

    @AfterEach
    void cleanupDatabase() {
        jdbcTemplate.update("DELETE FROM follow", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM comment", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM likes", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM processing", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM style", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM image", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM refresh_token", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM users", new MapSqlParameterSource());
    }

    private static void applyMigrations() throws LiquibaseException {
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword())) {

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    "migration/migration.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.update(new Contexts());
            log.info("Database migrations applied successfully");
        } catch (SQLException e) {
            log.error("Database connection failed", e);
            throw new RuntimeException(e);
        }
    }
}