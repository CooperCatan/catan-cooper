package catan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnectionManager {
    private final String url;
    private final Properties properties;

    public DatabaseConnectionManager(String host, String databaseName, String username, String password) {
        // Use environment variables if available, otherwise use provided values
        String dbHost = System.getenv("POSTGRES_HOST") != null ? System.getenv("POSTGRES_HOST") : host;
        String dbName = System.getenv("POSTGRES_DB") != null ? System.getenv("POSTGRES_DB") : databaseName;
        String dbUser = System.getenv("POSTGRES_USER") != null ? System.getenv("POSTGRES_USER") : username;
        String dbPass = System.getenv("POSTGRES_PASSWORD") != null ? System.getenv("POSTGRES_PASSWORD") : password;

        this.url = "jdbc:postgresql://" + dbHost + "/" + dbName;
        this.properties = new Properties();
        this.properties.setProperty("user", dbUser);
        this.properties.setProperty("password", dbPass);
    }

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(this.url, this.properties);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database. URL: " + this.url + ", User: " + this.properties.getProperty("user"));
            throw e;
        }
    }
}