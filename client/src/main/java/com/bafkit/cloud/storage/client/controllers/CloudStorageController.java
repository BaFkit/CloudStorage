package com.bafkit.cloud.storage.client.controllers;

import com.bafkit.cloud.storage.client.Client;
import com.bafkit.cloud.storage.client.controllers.utilities.WindowController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;


public class CloudStorageController implements Initializable, WindowController {

    private static CloudStorageController cloudStorageController;
    private final Client client;
    private final ObservableList<String> list;
    private String listFilesOnServer;

    public CloudStorageController() {
        client = Client.getClient();
        list = FXCollections.observableArrayList();
        cloudStorageController = this;
    }

    @FXML
    TextField pathField;
    @FXML
    ListView<String> cloudFilesList;
    @FXML
    TextArea fileInfoTextArea;
    @FXML
    TextField nameFolderField;
    @FXML
    TextField searchField;
    @FXML
    Button newFolder;
    @FXML
    Button download;
    @FXML
    Button upload;
    @FXML
    Button copy;
    @FXML
    Button cut;
    @FXML
    Button paste;
    @FXML
    Button delete;
    @FXML
    Button back;
    @FXML
    Button exit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            client.sendCommand("list");
            listFilesOnServer = client.readCommand();
            refreshListView(list, listFilesOnServer, cloudFilesList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshListView(ObservableList<String> list, String listFilesOnServer, ListView<String> cloudFilesList) {
        list.removeAll(this.list);
        cloudFilesList.getItems().clear();
        String[] files = listFilesOnServer.trim().split(" ");
        for (int i = 0; i < files.length; i++) {
            files[i] = files[i].replace("@", " ");
        }
        list.addAll("...");
        if (Arrays.asList(files).get(0).isEmpty()) {
            list.addAll("Empty");
        } else {
            list.addAll(files);
        }
        cloudFilesList.getItems().addAll(list);
        cloudFilesList.getItems().sort(String::compareTo);
        refreshPathField();
    }

    public void refreshPathField() {
        try {
            client.sendCommand("currentDir");
            pathField.setText(client.readCommand());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cd(String dir) throws IOException {
        if (dir.equals("...")) dir = "...";
        client.sendCommand("cd " + dir);
        client.readCommand();
        client.sendCommand("list");
        listFilesOnServer = client.readCommand();
        refreshListView(list, listFilesOnServer, cloudFilesList);
    }

    public void selectItem(MouseEvent mouseEvent) {
        String item;
        if (mouseEvent.getClickCount() >= 2 && !cloudFilesList.getSelectionModel().isEmpty()) {
            item = cloudFilesList.getSelectionModel().getSelectedItem().replace(" ", "@");
            try {
                cd(item.replace("[dir]@", "").replace("[file]@", ""));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mouseEvent.getClickCount() == 1 && !cloudFilesList.getSelectionModel().isEmpty()) {
            fileInfoTextArea.clear();
            item = cloudFilesList.getSelectionModel().getSelectedItem().replace(" ", "@");
            try {
                client.sendCommand("fileInfo " + item.replace("[dir]@", "").replace("[file]@", ""));
                String[] fileInfo = client.readCommand().trim().split(" ");
                if (fileInfo[0].equals("...")) return;
                fileInfoTextArea.appendText("Name:\n" + fileInfo[0].replace("@", " ") + "\n");
                if (fileInfo[1].equals("dir")) fileInfoTextArea.appendText("\nType:\n" + "Directory" + "\n");
                else fileInfoTextArea.appendText("\nSize:\n" + fileInfo[1] + " bytes\n");
                fileInfoTextArea.appendText("\nLast Modified Time:\n" + fileInfo[2] + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickUpload(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file");
        File uploadFile = fileChooser.showOpenDialog(exit.getScene().getWindow());
        if (uploadFile.getName().contains("@")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "The file name must not contain a symbol \"@\"");
            alert.showAndWait();
            return;
        }
        try {
            client.sendCommand("upload " + uploadFile.getName().replace(" ", "@"));
            String command = client.readCommand();
            if (command.equals("ready")) {
                client.sendCommand("waitingSend " + uploadFile.length());
                command = client.readCommand();
                if (command.equals("waitingGet")) {
                    client.sendFile(uploadFile.getAbsolutePath());
                }
                command = client.readCommand();                   //***
                System.out.println(command + "  загрузка файла");

                client.sendCommand("list");
                listFilesOnServer = client.readCommand();
                refreshListView(list, listFilesOnServer, cloudFilesList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickDownload(ActionEvent actionEvent) {
        if (!cloudFilesList.getSelectionModel().getSelectedItem().isEmpty()
                && !cloudFilesList.getSelectionModel().getSelectedItem().equals("...")) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select file");
            String nameFile = cloudFilesList.getSelectionModel().getSelectedItem().replace(" ", "@");
            File directory = fileChooser.showSaveDialog(exit.getScene().getWindow());
            try {
                client.sendCommand("download " + nameFile.replace("[dir]@", "").replace("[file]@", ""));
                String command = client.readCommand();
                if (command.split(" ")[0].equals("success")) {
                    long sizeFile = Long.parseLong(command.split(" ")[1]);
                    client.sendCommand("waitingGet");
                    boolean msg = client.getFile(directory.getPath(), nameFile, sizeFile);
                    System.out.println("Загрузка файла на компьютер - " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickCopy(ActionEvent actionEvent) {
        try {
            if (!cloudFilesList.getSelectionModel().getSelectedItem().isEmpty()
                    && !cloudFilesList.getSelectionModel().getSelectedItem().equals("...")) {
                String item = cloudFilesList.getSelectionModel().getSelectedItem().replace(" ", "@");
                client.sendCommand("copy " + item.replace("[dir]@", "").replace("[file]@", ""));
                String command = client.readCommand();
                if (command.equals("success")) {
                    paste.setStyle("-fx-text-fill: green");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickCut(ActionEvent actionEvent) {
        try {
            if (!cloudFilesList.getSelectionModel().getSelectedItem().isEmpty()
                    && !cloudFilesList.getSelectionModel().getSelectedItem().equals("...")) {
                String item = cloudFilesList.getSelectionModel().getSelectedItem().replace(" ", "@");
                client.sendCommand("cut " + item.replace("[dir]@", "").replace("[file]@", ""));
                String command = client.readCommand();
                if (command.equals("success")) {
                    paste.setStyle("-fx-text-fill: green");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void clickPaste(ActionEvent actionEvent) {

        try {
            client.sendCommand("paste");
            String command = client.readCommand();
            if (command.equals("success")) {
                paste.setStyle("-fx-text-fill: black");
                client.sendCommand("list");
                listFilesOnServer = client.readCommand();
                refreshListView(list, listFilesOnServer, cloudFilesList);
            } else {
                System.out.println("error");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickDelete(ActionEvent actionEvent) {
        try {
            if (!cloudFilesList.getSelectionModel().getSelectedItem().isEmpty()
                    && !cloudFilesList.getSelectionModel().getSelectedItem().equals("...")) {
                String item = cloudFilesList.getSelectionModel().getSelectedItem().replace(" ", "@");
                client.sendCommand("delete " + item.replace("[dir]@", "").replace("[file]@", ""));
                String command = client.readCommand();
                if (command.equals("success")) {
                    client.sendCommand("list");
                    listFilesOnServer = client.readCommand();
                    refreshListView(list, listFilesOnServer, cloudFilesList);
                } else {
                    System.out.println("error");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickNewFolder(ActionEvent actionEvent) {
        if (!nameFolderField.getText().isEmpty()) {
            if (nameFolderField.getText().contains("@")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "The directory name must not contain the symbol \"@\"");
                alert.showAndWait();
                return;
            }
            String nameNewFolder = nameFolderField.getText().replace(" ", "@");
            try {
                client.sendCommand("mkdir " + nameNewFolder);
                String command = client.readCommand();
                if (command.equals("success")) {
                    client.sendCommand("list");
                    nameFolderField.clear();
                    listFilesOnServer = client.readCommand();
                    refreshListView(list, listFilesOnServer, cloudFilesList);
                } else {
                    System.out.println("error");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickSearch(ActionEvent actionEvent) {
        openWindowSearch("search");
    }

    public void goToLocationOfFoundFile(String listFileLocation) {
        refreshListView(list, listFileLocation, cloudFilesList);
    }

    public void clickBack(ActionEvent actionEvent) {
        try {
            cd("...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickExit(ActionEvent actionEvent) {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Client.resetClient();
        changeWindow(exit.getScene(), "authentication");
    }

    public static CloudStorageController getCloudStorageController() {
        return cloudStorageController;
    }
}
