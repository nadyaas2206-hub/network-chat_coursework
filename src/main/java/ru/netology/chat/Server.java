package ru.netology.chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static int port;

    public static void main(String[] args) {
        port = readPortFromSettings();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);
            System.out.println("Ожидание клиентов...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Новый клиент подключился: " + socket.getInetAddress());

                ClientHandler handler = new ClientHandler(socket, clients);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    private static int readPortFromSettings() {
        int defaultPort = 8080;
        File file = new File("src/main/resources/settings.txt");
        if (!file.exists()) {
            System.out.println("Файл настроек не найден, использую порт " + defaultPort);
            return defaultPort;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("port=")) {
                    return Integer.parseInt(line.substring(5).trim());
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Ошибка чтения настроек, использую порт " + defaultPort);
        }
        return defaultPort;
    }
}
