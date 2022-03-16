package com.bafkit.cloud.storage.client.controllers;

import com.bafkit.cloud.storage.client.Client;
import com.bafkit.cloud.storage.client.controllers.utilities.WindowController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;


public class CloudStorageController implements Initializable, WindowController {

    private final Client client;
    private final ObservableList<String> list;
    private String listFilesOnServer;

    public CloudStorageController() {
        client = Client.getClient();
        list = FXCollections.observableArrayList();
    }


    @FXML
    ListView<String> cloudFilesList;

    @FXML
    Button upload;

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
            files[i] = files[i].replace("??", " ");
        }
        list.addAll("...");
        if (Arrays.asList(files).get(0).isEmpty()) {
            list.addAll("Empty");
        } else {
            list.addAll(files);
        }
        cloudFilesList.getItems().addAll(list);
    }

    public void cd(String dir) throws IOException {
        if (dir.equals("...")) dir = "...";
        client.sendCommand("cd " + dir);
        client.readCommand();
        client.sendCommand("list");
        listFilesOnServer = client.readCommand();
        refreshListView(list, listFilesOnServer, cloudFilesList);
    }


    public void clickUpload(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file");
        File uploadFile = fileChooser.showOpenDialog(exit.getScene().getWindow());
        try {
            client.sendCommand("upload " + uploadFile.getName());
            String command = client.readCommand();
            if (command.equals("ready")) {
                System.out.println(uploadFile.length());
                client.sendCommand("waitingSend " + uploadFile.length());
                command = client.readCommand();
                if (command.equals("waitingGet")) {
                    client.sendFile(uploadFile);
                }
                command = client.readCommand();
                System.out.println(command);
                client.sendCommand("list");
                listFilesOnServer = client.readCommand();
                refreshListView(list, listFilesOnServer, cloudFilesList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickExit(ActionEvent actionEvent) {
        changeWindow(exit.getScene(), "authentication");
    }

    public void selectItem(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() >= 2 && !cloudFilesList.getSelectionModel().getSelectedItem().isEmpty()) {
            String item = cloudFilesList.getSelectionModel().getSelectedItem();
            try {
                cd(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
