package com.bafkit.cloud.storage.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class ClientApp {

    private final String SERVER = "localhost";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public ClientApp (){
        try {
            this.socket = new Socket(SERVER, SERVER_PORT);
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendCommand(String command) throws IOException {
        out.write(command.getBytes());
        out.flush();
    }

    public String read() throws IOException {
        byte[] buffer = new byte[256];
        int bytesRead = in.read(buffer);
        return new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
    }

}


