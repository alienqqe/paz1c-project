 package org.openjfx.hellofx.utils;

 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import io.github.cdimascio.dotenv.Dotenv;

 public class Database {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = "jdbc:mysql://localhost:3306/gym_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = dotenv.get("DB_USER"); 
    private static final String PASSWORD = dotenv.get("DB_PASSWORD"); // replace with your DB password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
