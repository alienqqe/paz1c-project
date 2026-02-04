package org.openjfx.hellofx;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openjfx.hellofx.utils.Database;
import org.testcontainers.containers.MySQLContainer;


public abstract class TestContainers {

    private static MySQLContainer<?> mysql;

    @BeforeAll
    static void startContainer() {
        synchronized (TestContainers.class) {
            if (mysql != null && mysql.isRunning()) {
                return;
            }

            try {
                configureDockerApiVersion();

                mysql = new MySQLContainer<>("mysql:8.4.2")
                    .withDatabaseName("gymtest_db");
                mysql.start();
                applyInitSql(mysql);
                System.setProperty("DB_JDBC", mysql.getJdbcUrl());
                System.setProperty("DB_USER", mysql.getUsername());
                System.setProperty("DB_PASSWORD", mysql.getPassword());
                Database.overrideDataSourceForTests(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
            } catch (Exception ex) {
                Assumptions.assumeTrue(false, "Skipping DAO integration tests: " + ex.getMessage());
            }
        }
    }

    private static void applyInitSql(MySQLContainer<?> container) throws SQLException {
        String sql = loadInitSql();
        try (Connection conn = DriverManager.getConnection(
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword()
            );
            Statement statement = conn.createStatement()) {
            for (String part : sql.split(";")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
        }
    }

    private static String loadInitSql() {
        try (var in = TestContainers.class.getClassLoader().getResourceAsStream("db/init.sql")) {
            if (in != null) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
        }

        Path cwd = Paths.get(System.getProperty("user.dir", "."));
        Path[] candidates = new Path[] {
            cwd.resolve(Paths.get("src", "test", "resources", "db", "init.sql")),
            cwd.resolve(Paths.get("hellofx", "src", "test", "resources", "db", "init.sql"))
        };

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                try {
                    return Files.readString(candidate, StandardCharsets.UTF_8);
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to read init SQL from " + candidate, ex);
                }
            }
        }

        throw new IllegalStateException(
            "Could not find db/init.sql on the classpath or at: " + candidates[0] + ", " + candidates[1]
        );
    }

    private static void configureDockerApiVersion() {
        var current = System.getProperty("api.version");
        if (current != null && !current.isBlank()) {
            return;
        }

        try {
            var process = new ProcessBuilder("docker", "version", "--format", "{{.Server.APIVersion}}")
                    .redirectErrorStream(true)
                    .start();

            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return;
            }

            if (process.exitValue() != 0) {
                return;
            }

            var apiVersion = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (!apiVersion.isBlank()) {
                System.setProperty("api.version", apiVersion);
            }
        } catch (Exception ignored) {
        }
    }

    protected void clearTables() {
        var jdbc = Database.jdbc();

        jdbc.update("DELETE FROM visits");
        jdbc.update("DELETE FROM training_sessions");
        jdbc.update("DELETE FROM coach_specializations");
        jdbc.update("DELETE FROM coach_availability");
        jdbc.update("DELETE FROM users");
        jdbc.update("DELETE FROM memberships");
        jdbc.update("DELETE FROM discount_rules");
        jdbc.update("DELETE FROM coaches");
        jdbc.update("DELETE FROM specializations");
        jdbc.update("DELETE FROM clients");
    }

    @BeforeEach
    void setUpBase() {
        clearTables();

        var jdbc = Database.jdbc();
        jdbc.update("INSERT INTO clients (id, name, email, phone_number, last_discount_threshold_used) VALUES (1, 'Seed Client 1', 'seed.client1@test.com', '111', 0)");
        jdbc.update("INSERT INTO clients (id, name, email, phone_number, last_discount_threshold_used) VALUES (2, 'Seed Client 2', 'seed.client2@test.com', '222', 0)");

        jdbc.update("INSERT INTO coaches (id, name, email, phone_number) VALUES (1, 'Seed Coach 1', 'seed.coach1@test.com', '555')");
        jdbc.update("INSERT INTO coaches (id, name, email, phone_number) VALUES (2, 'Seed Coach 2', 'seed.coach2@test.com', '666')");
    }
}
