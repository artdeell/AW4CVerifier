package org.example.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DbCreatorInterface extends DbInterface {
    private static final BlockingQueue<DbCreatorInterface> creatorInterfaces = new LinkedBlockingQueue<>(MAX_CONNECTIONS);
    static {
        while (creatorInterfaces.remainingCapacity() != 0) creatorInterfaces.add(new DbCreatorInterface());
    }

    private PreparedStatement recordCreateStatement;
    private PreparedStatement subtractRegistrationStatement;
    private PreparedStatement prepareKeyStatement;
    private PreparedStatement enrollMessengerStatement;
    @Override
    public void connect() throws SQLException {
        super.connect();
        recordCreateStatement = connection.prepareStatement("INSERT INTO aw4c_users (SkyUserID,UserKey) VALUES (?,?);");
        prepareKeyStatement = connection.prepareStatement("INSERT INTO aw4c_keys (UserKey) VALUES (?);");
        subtractRegistrationStatement = connection.prepareStatement("UPDATE aw4c_keys SET RegistrationsRemaining = ? WHERE UserKey = ?;");
        enrollMessengerStatement = connection.prepareStatement("INSERT INTO aw4c_messengerInfo (MessengerID,MessengerType,UserKey) VALUES (?,?,?);");
    }

    public synchronized void createUserRecord(String skyUID, String userKey) throws SQLException {
        maintainConnection();
        recordCreateStatement.setString(1, skyUID);
        recordCreateStatement.setString(2, userKey);
        recordCreateStatement.execute();
    }

    public synchronized void saveUserRegistrations(String userKey, long regs) throws SQLException {
        maintainConnection();
        subtractRegistrationStatement.setLong(1, regs);
        subtractRegistrationStatement.setString(2, userKey);
        subtractRegistrationStatement.execute();
    }

    public synchronized void createKeyRecord(String uid) throws SQLException {
        maintainConnection();
        prepareKeyStatement.setString(1, uid);
        prepareKeyStatement.execute();
    }

    public synchronized void createMessengerRecord(long messengerId, byte messengerType, String key) throws SQLException  {
        maintainConnection();
        enrollMessengerStatement.setLong(1, messengerId);
        enrollMessengerStatement.setByte(2, messengerType);
        enrollMessengerStatement.setString(3, key);
        enrollMessengerStatement.execute();
    }

    public static DbCreatorInterface take() throws InterruptedException{
        return creatorInterfaces.take();
    }

    public static void giveBack(DbCreatorInterface dbCreatorInterface) {
        try {
            creatorInterfaces.put(dbCreatorInterface);
        }catch (InterruptedException e) {
            new RuntimeException("We stalled when returning the interface - SHOULD NEVER HAPPEN").printStackTrace();
            System.exit(1);
        }
    }
}
