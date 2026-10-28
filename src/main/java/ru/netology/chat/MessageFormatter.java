package ru.netology.chat;

public class MessageFormatter {
    public static String format(String username, String message) {
        return "[" + username + "]: " + message;
    }
}
