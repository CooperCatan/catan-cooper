package catan.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnectionManager {
    private final String host;
    private final String database;
    private final String username;
    private final String password;

    public DatabaseConnectionManager() {
        this.host = System.getenv().getOrDefault("POSTGRES_HOST", "localhost");
        this.database = System.getenv().getOrDefault("POSTGRES_DB", "postgres");
        this.username = System.getenv().getOrDefault("POSTGRES_USER", "postgres");
        this.password = System.getenv().getOrDefault("POSTGRES_PASSWORD", "password");
    }

    public Connection getConnection() throws SQLException {
        String url = String.format("jdbc:postgresql://%s:5432/%s", host, database);
        return DriverManager.getConnection(url, username, password);
    }
} 