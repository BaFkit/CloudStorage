package com.bafkit.cloud.storage.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Client implements Closeable {

    private static Client client;
    private final String SERVER = "localhost";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private String login;

    public Client() {
        try {
            this.socket = new Socket(SERVER, SERVER_PORT);
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Client getClient() {
        if (client == null) {
            client = new Client();
        }
        return client;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void sendCommand(String command) throws IOException {
        out.write(command.getBytes());
        out.flush();
    }

    public String readCommand() throws IOException {
        byte[] buffer = new byte[256];
        int bytesRead = in.read(buffer);
        return new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
    }

    public void sendFile(File uploadFile) {
        try {
            byte[] bytes = Files.readAllBytes(uploadFile.toPath());
            System.out.println(bytes.length);
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}


