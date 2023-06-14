package org.example.botimpl.telegram;

import org.json.JSONObject;

public interface LongPollListener {
    void handleUpdate(JSONObject update) throws InterruptedException;
}
