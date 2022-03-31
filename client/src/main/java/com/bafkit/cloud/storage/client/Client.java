package com.bafkit.cloud.storage.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Client implements Closeable {

    private static Client client;
    private final String SERVER = "localhost";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private String login;
    private byte[] bufferCommand;
    private byte[] bufferFile;

    public Client() {
        try {
            this.socket = new Socket(SERVER, SERVER_PORT);
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        bufferCommand = new byte[256];
        bufferFile = new byte[1024*1024*10];
    }

    public static Client getClient() {
        if (client == null) {
            client = new Client();
        }
        return client;
    }
    public static void resetClient() {
        client = null;
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
        int bytesRead = in.read(bufferCommand);
        return new String(bufferCommand, 0, bytesRead, StandardCharsets.UTF_8);
    }

    public int getNumberPartsOfFile(Path path) {
        try {
            long fileSize = Files.size(path);
            int partsCount = (int) (fileSize / bufferFile.length);
            if (fileSize % bufferFile.length != 0) {
                partsCount++;
            }
            return partsCount;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void sendFile(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            System.out.println(bytes.length);
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getFile(String path, String nameFile, long sizeFile){
        if (Files.exists(Paths.get(path + "/" + nameFile))) {
            nameFile = "copy_".concat(nameFile);
        }
        long countSize = 0;
        try {
            while (sizeFile != countSize) {
                byte[] buffer = new byte[65536];
                int bytesRead = in.read(buffer);
                countSize += bytesRead;
                byte[] tmp = new byte[bytesRead];
                System.arraycopy(buffer, 0, tmp, 0, tmp.length);
                if (!Files.exists(Paths.get(path))) {
                    Files.createFile(Paths.get(path));
                }
                Files.write(Paths.get(path), tmp, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}


