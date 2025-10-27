 package org.openjfx.hellofx.utils;

 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;

 public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/gym_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root"; // replace with your DB user
    private static final String PASSWORD = "97531908a"; // replace with your DB password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
