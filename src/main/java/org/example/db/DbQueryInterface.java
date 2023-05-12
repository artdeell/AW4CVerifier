package org.example.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DbQueryInterface extends DbInterface{
    private static final BlockingQueue<DbQueryInterface> queryInterfaces = new LinkedBlockingQueue<>(MAX_CONNECTIONS);
    static {
        while (queryInterfaces.remainingCapacity() != 0) queryInterfaces.add(new DbQueryInterface());
    }
    private PreparedStatement userQueryStatement;
    private PreparedStatement keyQueryStatement;
    private PreparedStatement botQueryStatement;

    @Override
    public void connect() throws SQLException {
        super.connect();
        userQueryStatement = connection.prepareStatement("SELECT * FROM aw4c_users WHERE SkyUserID=?");
        keyQueryStatement = connection.prepareStatement("SELECT * FROM aw4c_keys WHERE UserKey=?");
        botQueryStatement = connection.prepareStatement("SELECT * FROM aw4c_messengerInfo WHERE MessengerID=? AND MessengerType=?");
    }

    public synchronized String getUserKey(String skyUID) throws SQLException {
        maintainConnection();
        userQueryStatement.setString(1, skyUID);
        userQueryStatement.execute();
        ResultSet resultSet = userQueryStatement.getResultSet();
        if(!resultSet.next()) return null;
        return resultSet.getString(2);
    }

    public synchronized long getRemainingRegistrations(String key) throws SQLException {
        maintainConnection();
        keyQueryStatement.setString(1, key);
        keyQueryStatement.execute();
        ResultSet resultSet = keyQueryStatement.getResultSet();
        if(!resultSet.next()) return -1;
        return resultSet.getLong(2);
    }

    public synchronized String getUserKey(long userId, byte messengerType) throws SQLException{
        maintainConnection();
        botQueryStatement.setLong(1, userId);
        botQueryStatement.setByte(2, messengerType);
        botQueryStatement.execute();
        ResultSet resultSet = botQueryStatement.getResultSet();
        if(!resultSet.next()) return null;
        return resultSet.getString(3);
    }

    public static DbQueryInterface take() throws InterruptedException{
        return queryInterfaces.take();
    }

    public static void giveBack(DbQueryInterface dbQueryInterface) {
        try {
            queryInterfaces.put(dbQueryInterface);
        }catch (InterruptedException e) {
            new RuntimeException("We stalled when returning the interface - SHOULD NEVER HAPPEN").printStackTrace();
            System.exit(1);
        }
    }


}
