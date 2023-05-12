package org.example.botimpl.telegram;

import org.json.JSONObject;

import java.io.IOException;

public interface LongPollListener {
    void handleUpdate(JSONObject update) throws InterruptedException;
}
