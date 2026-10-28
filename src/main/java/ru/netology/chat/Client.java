package ru.netology.chat;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Client {
    private static String username;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Socket socket;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        int port = readPortFromSettings();

        try {
            socket = new Socket("localhost", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Введите ваше имя: ");
            username = console.readLine();
            out.println(username); // отправляем имя на сервер
            log("Вы вошли как " + username);

            Thread readerThread = new Thread(() -> {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        System.out.println(fromServer);
                        log("Получено: " + fromServer);
                    }
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Соединение с сервером потеряно.");
                    }
                } finally {
                    running = false;
                }
            });
            readerThread.start();

            String userInput;
            while (running && (userInput = console.readLine()) != null) {
                if ("/exit".equalsIgnoreCase(userInput.trim())) {
                    out.println("/exit");
                    log("Вы вышли из чата");
                    break;
                }
                out.println(userInput);
                log("Отправлено: " + userInput);
            }

        } catch (IOException e) {
            System.err.println("Не удалось подключиться к серверу: " + e.getMessage());
        } finally {
            running = false;
            try {
                if (socket != null) socket.close();
            } catch (IOException ignored) {}
        }
    }

    private static int readPortFromSettings() {
        return 8080; // замени на реальное чтение
    }

    private static void log(String message) {
        try (FileWriter fw = new FileWriter("client_file.log", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pw.println(time + " | " + message);
        } catch (IOException e) {
            System.err.println("Ошибка записи в клиентский лог: " + e.getMessage());
        }
    }
}
