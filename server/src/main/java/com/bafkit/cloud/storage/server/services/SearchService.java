package com.bafkit.cloud.storage.server.services;

import com.bafkit.cloud.storage.server.ActionController;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class SearchService extends SimpleFileVisitor<Path> {

    private final String searchFile;
    private final ActionController actionController;

    public SearchService(String searchFile, ActionController actionController) {
        this.searchFile = searchFile;
        this.actionController = actionController;
    }

    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.getFileName().toString().equals(searchFile)) {
            actionController.sendSearchFile(dir.toString());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.getFileName().toString().equals(searchFile)) {
            actionController.sendSearchFile(file.toString());
        }
        return FileVisitResult.CONTINUE;
    }
}
