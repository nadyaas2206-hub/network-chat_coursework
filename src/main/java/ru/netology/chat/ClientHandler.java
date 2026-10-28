package ru.netology.chat;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final List<ClientHandler> clients;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            username = in.readLine();
            if (username == null || username.isBlank()) {
                username = "Аноним";
            }
            System.out.println("Пользователь " + username + " вошёл в чат.");
            broadcast("[" + username + "] присоединился к чату", this);
            log("Пользователь " + username + " подключился");

            String message;
            while ((message = in.readLine()) != null) {
                if ("/exit".equalsIgnoreCase(message.trim())) {
                    break;
                }
                String formatted = "[" + username + "]: " + message;
                System.out.println(formatted); // выводим в консоль сервера
                log(formatted);                // пишем в файл
                broadcast(formatted, this);    // рассылаем другим
            }
        } catch (IOException e) {
            System.err.println("Ошибка при обслуживании " + username + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void broadcast(String message, ClientHandler except) {
        for (ClientHandler client : clients) {
            if (client != except) {
                client.sendMessage(message);
            }
        }
    }

    private void log(String message) {
        try (FileWriter fw = new FileWriter("file.log", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pw.println(time + " | " + message);
        } catch (IOException e) {
            System.err.println("Не удалось записать в лог: " + e.getMessage());
        }
    }

    private void disconnect() {
        clients.remove(this);
        broadcast("[" + username + "] покинул чат", this);
        log("Пользователь " + username + " отключился");
        try {
            socket.close();
        } catch (IOException e) {
            // ничего не делаем
        }
    }
}
