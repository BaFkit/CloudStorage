package com.bafkit.cloud.storage.server.services;

public interface AuthorizationService {
    void start();
    void stop();
    String registration(String login, int pass);
    long checkUserVerification(String login, String password);
    String getRootClient(String login, String password);
}
