package com.bafkit.cloud.storage.server;

import com.bafkit.cloud.storage.server.services.AuthorizationService;
import com.bafkit.cloud.storage.server.services.CopyService;
import com.bafkit.cloud.storage.server.services.DeleteService;
import com.bafkit.cloud.storage.server.services.SearchService;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class ActionController {

    private final String root;
    private String rootClient;
    private String currentDir;
    private String pathUp;
    private long spaceClient;
    private String nameUploadFile;

    private File fileUpload;
    private File fileDownload;

    private String nameCopyFile = " ";
    private Path copyPathFile;
    private StringBuilder findFiles;
    private boolean flagCut = false;
    private boolean flagAppend = false;
    private int partsCountClient;
    private int partsCountServer;
    private OutputStream fos;

    private final AuthorizationService authorizationService;


    public ActionController(AuthorizationService authorizationService, String root) {
        this.authorizationService = authorizationService;
        this.root = root;
        this.rootClient = root;
        this.currentDir = root;
        this.pathUp = root;
    }

    public String authorization(String[] parts) {
        spaceClient = authorizationService.checkUserVerification(parts[1], parts[2]);
        if (spaceClient > -1) {
            rootClient = authorizationService.getRootClient(parts[1], parts[2]);
            if (rootClient.equals("notExist")) {
                String folderClient = "folder_" + parts[1];
                File folder = new File(root + File.separator + folderClient);
                folder.mkdir();
                rootClient = parts[1];
            } else {
                File folder = new File(root + File.separator + rootClient);
                if (!folder.exists()) {
                    folder.mkdir();
                }
            }
            currentDir = currentDir.concat("/").concat(rootClient);
            rootClient = currentDir;
            pathUp = currentDir;
            return "success";
        }
        return "unSuccess";
    }

    public String registration(String[] parts) {
        String login = parts[1];
        int pass = parts[2].hashCode();
        String reg = authorizationService.registration(login, pass);
        if (reg.equals("busy")) {
            return "busy";
        }
        if (reg.equals("success")) {
            String folderClient = "folder_" + login;
            File folder = new File(root + File.separator + folderClient);
            if (!folder.exists()) {
                folder.mkdir();
            }
            return "success";
        } else {
            return "unSuccess";
        }
    }

    public String mkdir(String[] parts) {
        System.out.println("Принята комманда mkdir");
        File folder = new File(currentDir + File.separator + parts[1].replace("@", " "));
        if (!folder.exists()) {
            folder.mkdir();
            return "success";
        } else {
            return "unSuccess";
        }
    }

    public String list() {
        System.out.println("Принята комманда list");
        File file = new File(currentDir);
        File[] files = file.listFiles();
        StringBuilder sb = new StringBuilder();
        assert files != null;
        for (File f : files) {
            if (f.isDirectory()) sb.append("[dir]@");
            else sb.append("[file]@");
            sb.append(f.getName().replace(" ", "@")).append(" ");
        }
        if (sb.length() < 1) sb.append("Empty");
        return sb.toString();
    }

    public String getCurrentDir() {
        return currentDir;
    }

    public String getPathUp(String dir) {
        if (dir.equals(rootClient)) return dir;
        int index = -1;
        for (int i = 0; i < dir.length(); i++) {
            if (dir.charAt(i) == '/') {
                index = i;
            }
        }
        dir = dir.substring(0, index);
        return dir;
    }

    public String cd(String part) {
        if (part.equals("...")) {
            currentDir = pathUp;
            pathUp = getPathUp(currentDir);
            return "success";
        } else {
            File cd = new File(currentDir + File.separator + part.replace("@", " "));
            if (cd.exists() && cd.isDirectory()) {
                currentDir = currentDir + "/" + part.replace("@", " ");
                pathUp = getPathUp(currentDir);
                return "success";
            } else {
                return "unSuccess";
            }
        }
    }


    public String upload(String[] parts) {
        fileUpload = new File(currentDir + File.separator + parts[1].replace("@", " "));
        if (fileUpload.exists()) {
            nameUploadFile = "copy_".concat(parts[1]);
        } else {
            nameUploadFile = parts[1];
        }
        return "ready";
    }
    public String uploadFile(byte[] bytes) {
        nameUploadFile = nameUploadFile.replace("@", " ");
        if (!Files.exists(Paths.get(currentDir + "/" + nameUploadFile))) {
            try {
                Files.createFile(Paths.get(currentDir + "/" + nameUploadFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.write(Paths.get(currentDir + "/" + nameUploadFile), bytes, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            return "unSuccess";
        }
        return "success";
    }
    public String checkCapacity(String size) {
        if (spaceClient > Long.parseLong(size)) {
            return "waitingGet";
        }
        return "exceeded";
    }

    public String download(String[] parts) {
        fileDownload = new File(currentDir + File.separator + parts[1].replace("@", " "));
        if (fileDownload.exists() && !fileDownload.isDirectory()) {
            return "success " + fileDownload.length();
        } else {
            return "unSuccess";
        }
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[512];
        try {
            bytes = Files.readAllBytes(fileDownload.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public String copy(String part) {
        copyPathFile = Paths.get(currentDir + "/" + part.replace("@", " "));
        nameCopyFile = part;
        return "success";
    }

    public String cut(String part) {
        String command = copy(part);
        if (command.equals("success")) {
            flagCut = true;
        }
        return command;
    }

    public String paste() {
        if (!nameCopyFile.equals(" ")){
            Path destination = Paths.get(currentDir + "/" + nameCopyFile);
            try {
                Files.walkFileTree(copyPathFile, new CopyService(copyPathFile, destination));
                if (flagCut) {
                    Files.walkFileTree(copyPathFile, new DeleteService());
                    flagCut = false;
                }
                nameCopyFile = " ";
                copyPathFile = null;
                return "success";
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        return "unSuccess";
    }

    public String delete(String part) {
        Path deleteFile = Paths.get(currentDir + "/" + part.replace("@", " "));
        try {
            Files.walkFileTree(deleteFile, new DeleteService());
            return "success";
        } catch (IOException e) {
            e.printStackTrace();
            return "unSuccess";
        }
    }

    public String search(String part) {
        part = part.replace("@", " ");
        Path searchPath = Paths.get(rootClient);
        findFiles = new StringBuilder();
        try {
            Files.walkFileTree(searchPath, new SearchService(part, this));
            return findFiles.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "unSuccess";
        }
    }

    public void sendSearchFile(String findFile) {
        this.findFiles.append(findFile.replace(" ", "@")).append(" ");
    }

    public String goToFile(String part) {
        Path selectedFile = Paths.get(part);
        if (!Files.isDirectory(selectedFile)) {
            selectedFile = selectedFile.getParent();
            System.out.println(selectedFile.toString());
        }
        currentDir = selectedFile.toString().replace("@", " ");
        return list();
    }

    public String getFileInfo(String part) {
        Path selectedFile = Paths.get(currentDir + "/" + part.replace("@", " "));
        StringBuilder sb = new StringBuilder();
        try {
            if (!Files.isDirectory(selectedFile)) {
                sb.append(part.replace(" ", "@")).append(" ");
                sb.append(Files.getAttribute(selectedFile, "size")).append(" ");
                sb.append(Files.getAttribute(selectedFile, "lastModifiedTime")).append(" ");
            } else {
                sb.append(part.replace(" ", "@")).append(" ");
                sb.append("dir").append(" ");
                sb.append(Files.getAttribute(selectedFile, "lastModifiedTime")).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}

