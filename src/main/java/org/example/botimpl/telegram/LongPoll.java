package org.example.botimpl.telegram;

import org.example.Utils;
import org.example.botimpl.BotConstants;
import org.example.botimpl.Telegram;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class LongPoll implements Runnable {
    private long offset = -1;
    private final LongPollListener longPollListener;
    public LongPoll(LongPollListener longPollListener) {
        this.longPollListener = longPollListener;
    }

    @Override
    public void run() {
        try {
            while(!Thread.interrupted()) {
                try {
                    runLoop();
                } catch (IOException e) {
                    e.printStackTrace();
                    Thread.sleep(5000);
                }
            }
        }catch (InterruptedException ignored) {}
    }

    private void runLoop() throws IOException, InterruptedException {
        JSONObject response = Utils.doGet(Telegram.formatRequest("getUpdates?timeout=20"+(offset < 0 ? "":"&offset="+offset)));
        if(!response.optBoolean("ok"))
            throw new IOException("Non-okay response from updates, description: "+response.optString("description"));
        JSONArray updates = response.getJSONArray("result");
        for(Object o : updates) {
            JSONObject update = (JSONObject) o;
            offset = update.getLong("update_id")+1;
            longPollListener.handleUpdate(update);
        }

    }

    private void handleUpdate(JSONObject update) {
        JSONObject message = update.optJSONObject("message");
        if(message == null) return;
        JSONObject from = message.getJSONObject("from");


    }
}
