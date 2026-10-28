package ru.netology.chat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageFormatterTest {
    @Test
    void testFormat() {
        String username = "Anya";
        String message = "Hello!";

        String expected = "[Anya]: Hello!";

        String actual = MessageFormatter.format(username, message);

        assertEquals(expected, actual, "Метод должен правильно склеивать имя и сообщение");
    }
}
