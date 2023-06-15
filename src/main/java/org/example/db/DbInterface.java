package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbInterface {
    public static final int MAX_CONNECTIONS=12;
    public static final String DATABASE_URL = "jdbc:mysql://localhost:3306/aw4c_verification?user=aw4c&password=aw4cpwd";
    protected Connection connection;
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(DATABASE_URL);
    }

    public void maintainConnection() throws SQLException {
        if (connection == null || connection.isClosed()) connect();
    }
}
