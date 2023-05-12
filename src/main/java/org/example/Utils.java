package org.example;

import org.example.db.DbCreatorInterface;
import org.example.db.DbQueryInterface;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Random;

public class Utils {
    public static final Random random = new Random();
    public static String generateRandomKey() {
        StringBuilder builder = new StringBuilder();
        while(builder.length() < 40) {
            builder.append(Integer.toHexString(random.nextInt()));
        }
        return builder.substring(0, 40);
    }

    public static String enrollRandomKey(DbQueryInterface queryInterface, DbCreatorInterface creatorInterface) throws SQLException {
        String newKey = Utils.generateRandomKey();
        while(queryInterface.getRemainingRegistrations(newKey) != -1) {
            newKey = Utils.generateRandomKey();
        }
        creatorInterface.createKeyRecord(newKey);
        return newKey;
    }
    public static JSONObject doGet(String url) throws IOException {
        try (InputStream conn = new URL(url).openStream()) {
            return new JSONObject(new String(conn.readAllBytes()));
        }
    }
}
