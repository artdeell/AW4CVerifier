package org.example.botimpl.telegram;

import org.example.Utils;
import org.example.botimpl.Telegram;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestQueue implements Runnable {
    private static final LinkedBlockingQueue<String> requestSendQueue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    Utils.doGet(Telegram.formatRequest(requestSendQueue.take()));
                } catch (IOException e) {
                    e.printStackTrace();
                    Thread.sleep(5000);
                }
            }
        }catch (InterruptedException ignored){}
    }

    public static void post(String request) {
        requestSendQueue.offer(request); // will never return false
    }
}
