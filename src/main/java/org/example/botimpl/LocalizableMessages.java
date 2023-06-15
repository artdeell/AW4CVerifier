package org.example.botimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LocalizableMessages {
    public static final int WELCOME = 0;
    public static final int PHONE_NUMBER_BTN_TEXT = 1;
    public static final int SQL_ERROR = 2;
    public static final int PRIVATE_CHAT_ONLY = 3;
    public static final int INVALID_CONTACT = 4;
    public static final int THANK_YOU = 5;
    public static final int WELCOME_BACK = 6;
    public static final int NO_KEY_YET = 7;
    public static final int REGISTRATIONS_REMAINING = 8;
    private static final Map<String, String[]> localeMap = new HashMap<>();
    private static final String[] defaultMessages = new String[] {
            "Welcome! Please press the button below to get yourself a key. Your phone number will not be stored and is only used for one-time verification.",
            "Share your phone number",
            "A data access error has occured. Contact the administrator.",
            "This bot may only be used through direct messages.",
            "Sorry, this contact is not the right one. Please use the \"Share your phone number\" button to continue",
            "Thank you! Your key is `%s`\nNote: You can use this key for only one Sky account\nWant more? Contact me!\nTelegram: @artdeell\nDiscord: artDev#7380",
            "Hi again! In case if you forgot, your key is `%s`",
            "Can't tell you that yet! You need to make a key first.",
            "You have %d registrations left.\nWant more from AW4C? DM me!\nTelegram: @artdeell\nDiscord: artDev#7380"
    };

    static {
        loadLocales();
    }

    public static void loadLocales() {
        File localeDirectory = new File("./locales/");
        File[] localeFiles = localeDirectory.listFiles();
        if(localeFiles != null) {
            System.out.println("Loading "+localeFiles.length+ " locales");
            for(File file : localeFiles) {
                loadSingleLocale(file);
            }
        }else{
            System.out.println("Failed to list locales");
        }
    }
    private static void loadSingleLocale(File locale) {
        try(FileInputStream fileInputStream = new FileInputStream(locale)) {
            String[] localeStringArray = new String(fileInputStream.readAllBytes()).split("\n");
            for(int i = 0; i < localeStringArray.length; i++) {
                localeStringArray[i] = localeStringArray[i].replace("$NL", "\n");
            }
            localeMap.put(locale.getName(), localeStringArray);
        }catch (IOException e) {
            System.out.println("Failed to load locale "+locale.getName() +": "+e.getMessage());
        }
    }
    private static String[] getLocalizedMessages(String locale) {
        String[] localeMsgs = localeMap.get(locale);
        if(localeMsgs == null) localeMsgs = defaultMessages;
        return localeMsgs;
    }
    public static String getString(String locale, int string, Object... formatArgs) {
        return String.format(getLocalizedMessages(locale)[string], formatArgs);
    }
    public static String getString(String locale, int string) {
        return getLocalizedMessages(locale)[string];
    }
}
