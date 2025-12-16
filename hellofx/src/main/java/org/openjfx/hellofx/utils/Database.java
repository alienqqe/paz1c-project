package org.openjfx.hellofx.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class Database {
    private static final Dotenv dotenv = Dotenv.load();

   
    private static final String PORT = 
            (dotenv.get("DB_PORT") == null || dotenv.get("DB_PORT").isBlank())
                    ? "3306"
                    : dotenv.get("DB_PORT");

    private static final String URL = 
            "jdbc:mysql://localhost:" + PORT + "/gym_db?allowPublicKeyRetrieval=true&useSSL=false";

    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        System.out.println("URL = " + URL);
        System.out.println("USER = " + USER);
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}