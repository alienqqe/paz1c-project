package org.openjfx.hellofx.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.openjfx.hellofx.utils.Database;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * Base class for DAO integration tests.
 * Starts a MySQL Testcontainer once and cleans tables before each test.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TestContainers {

    private static MySQLContainer<?> mysql;

    @BeforeAll
    static void startContainer() {
        try {
            mysql = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("gym_db")
                .withUsername("testuser")
                .withPassword("testpass")
                .withCopyFileToContainer(
                    MountableFile.forClasspathResource("db/init.sql"),
                    "/docker-entrypoint-initdb.d/init.sql"
                )
                .withReuse(true);
            mysql.start();

            // Point application datasource to the container
            Database.overrideDataSourceForTests(
                mysql.getJdbcUrl(),
                mysql.getUsername(),
                mysql.getPassword()
            );
        } catch (Exception ex) {
            // If Docker is unavailable, skip integration tests instead of failing the build.
            Assumptions.assumeTrue(false, "Skipping DAO integration tests: " + ex.getMessage());
        }
    }

    /**
     * Clears all tables to give each test a clean slate.
     */
    protected void clearTables() {
        var jdbc = Database.jdbc();
        jdbc.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbc.update("TRUNCATE TABLE visits");
        jdbc.update("TRUNCATE TABLE training_sessions");
        jdbc.update("TRUNCATE TABLE coach_specializations");
        jdbc.update("TRUNCATE TABLE coach_availability");
        jdbc.update("TRUNCATE TABLE memberships");
        jdbc.update("TRUNCATE TABLE users");
        jdbc.update("TRUNCATE TABLE discount_rules");
        jdbc.update("TRUNCATE TABLE coaches");
        jdbc.update("TRUNCATE TABLE specializations");
        jdbc.update("TRUNCATE TABLE clients");
        jdbc.execute("SET FOREIGN_KEY_CHECKS=1");
    }

    @BeforeEach
    void setUpBase() {
        clearTables();
    }

    @AfterAll
    static void stopContainer() {
        if (mysql != null) {
            mysql.stop();
        }
    }
}
