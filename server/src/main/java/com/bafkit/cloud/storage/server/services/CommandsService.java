package com.bafkit.cloud.storage.server.services;

import com.bafkit.cloud.storage.server.ActionController;
import com.bafkit.cloud.storage.server.MainHandler;

public class CommandsService {

    private final ActionController actionController;
    private final MainHandler mainHandler;

    public CommandsService(ActionController actionController, MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.actionController = actionController;
    }

    public String executeCommand(String cmd, String[] parts) {
        String msgSend;
        switch (cmd) {
            case ("auth"):
                return msgSend = actionController.authorization(parts);
            case ("reg"):
                return msgSend = actionController.registration(parts);
            case ("list"):
                return msgSend = actionController.list();
            case ("currentDir"):
                return msgSend = actionController.getCurrentDir();
            case ("cd"):
                return msgSend = actionController.cd(parts[1]);
            case ("mkdir"):
                return msgSend = actionController.mkdir(parts);
            case ("upload"):
                msgSend = actionController.upload(parts);
                mainHandler.setUploadFileSize(Long.parseLong(parts[2]));
                if (msgSend.equals("ready")) {
                    mainHandler.setUploadFlag(true);
                }
                return msgSend;
            case ("download"):
                return msgSend = actionController.download(parts);
            case ("waitingGet"):
                mainHandler.setDownloadFlag(true);
                return msgSend = "startDownload";
            case ("copy"):
                return msgSend = actionController.copy(parts[1]);
            case ("paste"):
                return msgSend = actionController.paste();
            case ("cut"):
                return msgSend = actionController.cut(parts[1]);
            case ("delete"):
                return msgSend = actionController.delete(parts[1]);
            case ("search"):
                return msgSend = actionController.search(parts[1]);
            case ("goToFile"):
                return msgSend = actionController.goToFile(parts[1]);
            case ("fileInfo"):
                return msgSend = actionController.getFileInfo(parts[1]);
            default:
                return msgSend = "unknown";
        }
    }
}
