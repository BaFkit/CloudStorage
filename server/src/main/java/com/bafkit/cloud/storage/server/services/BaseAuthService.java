package com.bafkit.cloud.storage.server.services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthorizationService {

    private final List<Entry> entries;
    private static Connection connection;
    private static PreparedStatement ps;

    public BaseAuthService() {
        entries = new ArrayList<>();
    }

    private Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:users_cloud.db");
            ps = connection.prepareStatement("SELECT * FROM users");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entries.add(new Entry(rs.getString("login"), rs.getInt("pass"), rs.getString("root"), rs.getInt("space")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (ps != null) {
                ps.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long checkUserVerification(String login, String password) {
        int pass = password.hashCode();
        long space = -1;
        for (Entry a : entries) {
            if (a.login.equals(login) && a.pass == pass) {
                space = a.space;
                return space;
            }
        }
        return space;
    }

    @Override
    public String registration(String login, int pass) {
        if (checkLogin(login)) {
            return "busy";
        }
        String folderClient = "folder_" + login;
        int spaceClient = 1000000000;
        try {
            ps = connection.prepareStatement("INSERT INTO users ('login', 'pass', 'root', 'space') VALUES" +
                    "(?, ?, ?, ?)");
            ps.setString(1, login);
            ps.setInt(2, pass);
            ps.setString(3, folderClient);
            ps.setInt(4, spaceClient);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return "unSuccess";
        }
        entries.add(new Entry(login, pass, folderClient, spaceClient));
        return "success";
    }

    public boolean checkLogin(String login) {
        for (Entry a : entries) {
            if (a.login.equals(login)) {
                return true;
            }
        }
        return false;
    }


    public String getRootClient(String login, String password) {
        int pass = password.hashCode();
        for (Entry a : entries) {
            if (a.login.equals(login) && a.pass == pass) {
                return a.root;
            }
        }
        return "notExist";
    }


    @Override
    public void start() {
        connect();
        System.out.println("AuthorizationService started");
    }

    @Override
    public void stop() {
        disconnect();
    }

    private static class Entry {
        private final String login;
        private final int pass;
        private final String root;
        private final int space;


        public Entry(String login, int pass, String root, int space) {
            this.login = login;
            this.pass = pass;
            this.root = root;
            this.space = space;
        }
    }
}

