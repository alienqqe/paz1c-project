package org.openjfx.hellofx.dao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.openjfx.hellofx.utils.Database;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TestContainers {

    @Container
    protected static final MySQLContainer<?> mysql =
        new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("gym_db")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("db/init.sql");

    @BeforeAll
    void setupDataSource() {
        mysql.start();
        Database.overrideDataSourceForTests(
            mysql.getJdbcUrl(),
            mysql.getUsername(),
            mysql.getPassword()
        );
    }
}
