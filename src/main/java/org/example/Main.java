package org.example;

import org.example.botimpl.Discord;
import org.example.botimpl.Telegram;
import org.example.server.HttpServer;


public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("org.example.patterns.PatternStorage");
        HttpServer.init();
        Telegram.start();
        Discord.start();
    }
}