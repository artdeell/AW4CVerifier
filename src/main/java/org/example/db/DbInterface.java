package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbInterface {
    public static final int MAX_CONNECTIONS=12;
    public static final String DATABASE_URL = "jdbc:mysql://sql.freedb.tech:3306/freedb_aw4c-verification?user=freedb_aw4c-verification&password=NQqaX$pRDY98b54";
    protected Connection connection;
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(DATABASE_URL);
    }

    public void maintainConnection() throws SQLException {
        if (connection == null || connection.isClosed()) connect();
    }
}
