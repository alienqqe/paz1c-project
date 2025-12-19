package org.openjfx.hellofx.utils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.cdimascio.dotenv.Dotenv;

public class Database {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String PORT =
        (dotenv.get("DB_PORT") == null || dotenv.get("DB_PORT").isBlank())
            ? "3306"
            : dotenv.get("DB_PORT");

    private static final String URL =
        "jdbc:mysql://127.0.0.1:" + PORT + "/gym_db?allowPublicKeyRetrieval=true&useSSL=false";

    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    // we use hikari data source, because it is faster, and manages the pooling.
    private static final HikariDataSource dataSource;
    private static final JdbcTemplate jdbcTemplate;

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
}
