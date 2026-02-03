package org.openjfx.hellofx.utils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.cdimascio.dotenv.Dotenv;

public class Database {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private static final String PORT =
        firstNonBlank(System.getProperty("DB_PORT"), dotenv.get("DB_PORT"), "3306");

    private static final String DEFAULT_URL =
        "jdbc:mysql://127.0.0.1:" + PORT + "/gym_db?allowPublicKeyRetrieval=true&useSSL=false";

    private static final String URL =
        firstNonBlank(System.getProperty("DB_JDBC"), DEFAULT_URL);

    private static final String USER =
        firstNonBlank(System.getProperty("DB_USER"), dotenv.get("DB_USER"));

    private static final String PASSWORD =
        firstNonBlank(System.getProperty("DB_PASSWORD"), dotenv.get("DB_PASSWORD"));

    // we use hikari data source, because it is faster, and manages the pooling.
    private static HikariDataSource dataSource;
    private static JdbcTemplate jdbcTemplate;

    static {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(URL);
        cfg.setUsername(USER);
        cfg.setPassword(PASSWORD);
        cfg.setMaximumPoolSize(10);
        cfg.setPoolName("gym_db_pool");
        dataSource = new HikariDataSource(cfg);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static JdbcTemplate jdbc() {
        return jdbcTemplate;
    }

    // method to prevent errors, if some code is still using manual jdbc
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Test-only hook: allows Testcontainers to supply its JDBC URL/credentials.
     * Rebuilds the underlying HikariDataSource and JdbcTemplate.
     */
    public static synchronized void overrideDataSourceForTests(String jdbcUrl, String user, String pass) {
        if (dataSource != null) {
            dataSource.close();
        }
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setMaximumPoolSize(5);
        cfg.setPoolName("gym_db_pool_test");
        HikariDataSource testDs = new HikariDataSource(cfg);
        dataSource = testDs;
        jdbcTemplate = new JdbcTemplate(testDs);
    }

    private static String firstNonBlank(String value, String fallback) {
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        return fallback;
    }

    private static String firstNonBlank(String first, String second, String fallback) {
        return firstNonBlank(first, firstNonBlank(second, fallback));
    }
}
