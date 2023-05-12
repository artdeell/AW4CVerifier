package org.example.botimpl;

import org.example.Utils;
import org.example.botimpl.telegram.LongPoll;
import org.example.botimpl.telegram.LongPollListener;
import org.example.botimpl.telegram.RequestQueue;
import org.example.db.DbCreatorInterface;
import org.example.db.DbQueryInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class Telegram implements LongPollListener {

    private static final String phoneNumberKeyboard;
    private static final String removeKeyboard = "{\"remove_keyboard\": true}";
    static {
        JSONObject sharePhoneButton = new JSONObject();
        sharePhoneButton.put("text", "BUTTON_TEXT");
        sharePhoneButton.put("request_contact", true);
        JSONArray keysArray = new JSONArray();
        JSONArray keysArray2 = new JSONArray();
        keysArray.put(sharePhoneButton);
        keysArray2.put(keysArray);
        JSONObject keyboard = new JSONObject();
        keyboard.put("keyboard", keysArray2);
        phoneNumberKeyboard = keyboard.toString();
    }
    public static void start() {
        new Thread(new LongPoll(new Telegram())).start();
        new Thread(new RequestQueue()).start();
    }
    public void sendMessage(String locale, long chatId, int localizableMessage) {
        RequestQueue.post("sendMessage?parse_mode=Markdown&chat_id="+chatId+
                "&text="+ URLEncoder.encode(LocalizableMessages.getString(locale, localizableMessage), StandardCharsets.UTF_8));

    }

    public void sendMessageV(String locale, long chatId, int localizableMessage, Object... va) {
        RequestQueue.post("sendMessage?parse_mode=Markdown&chat_id="+chatId+
                "&text="+ URLEncoder.encode(LocalizableMessages.getString(locale, localizableMessage, va), StandardCharsets.UTF_8)
        );
    }

    public void sendMessageKV(String locale, long chatId, int localizableMessage, String keyboard, Object... va) {
        RequestQueue.post("sendMessage?parse_mode=Markdown&chat_id="+chatId+
                "&text="+ URLEncoder.encode(LocalizableMessages.getString(locale, localizableMessage, va), StandardCharsets.UTF_8)+
                "&reply_markup="+URLEncoder.encode(keyboard, StandardCharsets.UTF_8)
        );

    }

    public void sendMessage(String locale, long chatId, int localizableMessage, String replyKeyboard) {
        RequestQueue.post("sendMessage?parse_mode=Markdown&chat_id="+chatId+
                "&text="+ URLEncoder.encode(LocalizableMessages.getString(locale, localizableMessage), StandardCharsets.UTF_8)+
                "&reply_markup="+URLEncoder.encode(replyKeyboard, StandardCharsets.UTF_8));
    }

    @Override
    public void handleUpdate(JSONObject update) throws InterruptedException{
        JSONObject message = update.optJSONObject("message");
        if(message != null) handleMessage(message);
    }

    public void handleMessage(JSONObject message) throws InterruptedException {
        JSONObject from = message.getJSONObject("from");
        long userId = from.getLong("id");
        JSONObject chat = message.getJSONObject("chat");
        long chatId = chat.getInt("id");
        String userLocale = from.optString("language_code", "en");
        if(!chat.getString("type").equals("private")) {
            sendMessage(userLocale, chatId, LocalizableMessages.PRIVATE_CHAT_ONLY);
            return;
        }
        DbQueryInterface queryInterface = DbQueryInterface.take();
        try {
            String key = queryInterface.getUserKey(userId, BotConstants.BOT_TYPE_TELEGRAM);
            if(key == null) {
                if(message.has("contact")) {
                    JSONObject contact = message.getJSONObject("contact");
                    if(contact.getString("first_name").equals(from.getString("first_name"))) {
                        DbCreatorInterface creatorInterface = DbCreatorInterface.take();
                        String newKey = Utils.enrollRandomKey(queryInterface, creatorInterface);
                        creatorInterface.createMessengerRecord(userId, BotConstants.BOT_TYPE_TELEGRAM, newKey);
                        sendMessageKV(userLocale, chatId, LocalizableMessages.THANK_YOU, removeKeyboard, newKey);
                        DbCreatorInterface.giveBack(creatorInterface);
                    }else{
                        sendMessage(userLocale, chatId, LocalizableMessages.INVALID_CONTACT);
                    }
                } else {
                    sendMessage(userLocale, chatId, LocalizableMessages.WELCOME,
                            phoneNumberKeyboard.replace("BUTTON_TEXT", LocalizableMessages.getString(userLocale, LocalizableMessages.PHONE_NUMBER_BTN_TEXT)));
                }
            }else{
                String text = message.optString("text");
                if(text != null && text.startsWith("/get_remaining_registrations")) {
                    long usageCount = queryInterface.getRemainingRegistrations(key);
                    if(usageCount < 0) {
                        sendMessage(userLocale, chatId, LocalizableMessages.SQL_ERROR);
                    }else {
                        sendMessageV(userLocale, chatId, LocalizableMessages.REGISTRATIONS_REMAINING, usageCount);
                    }
                }else {
                    sendMessageV(userLocale, chatId, LocalizableMessages.WELCOME_BACK, key);
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
            sendMessage(userLocale, chatId, LocalizableMessages.SQL_ERROR);
        }
        DbQueryInterface.giveBack(queryInterface);
    }

    public static String formatRequest(String requestUrl) {
        return "https://api.telegram.org/bot" + BotConstants.TELEGRAM_TOKEN + "/" + requestUrl;
    }
}
