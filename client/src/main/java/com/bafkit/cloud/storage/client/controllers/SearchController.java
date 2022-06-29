package com.bafkit.cloud.storage.client.controllers;

import com.bafkit.cloud.storage.client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SearchController implements Initializable {
    private final Client client;
    private final ObservableList<String> listSearch;
    private final CloudStorageController cloudStorageController;


    public SearchController() {
        client = Client.getClient();
        cloudStorageController = CloudStorageController.getCloudStorageController();
        listSearch = FXCollections.observableArrayList();
    }

    @FXML
    Button findButton;
    @FXML
    TextField searchTextField;
    @FXML
    ListView<String> searchListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!cloudStorageController.searchField.getText().isEmpty()) {
            findFile(cloudStorageController.searchField.getText());
            cloudStorageController.searchField.clear();
        }
    }

    public void clickFindButton(ActionEvent actionEvent) {
        String neededFile = searchTextField.getText().replace(" ", "@");
        findFile(neededFile);
    }

    public void findFile(String neededFile) {
        try {
            client.sendCommand("search " + neededFile);
            String foundFileList = client.readCommand();
            refreshSearchListView(listSearch, foundFileList, searchListView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshSearchListView(ObservableList<String> listSearch, String foundFileList, ListView<String> searchListView) {
        searchListView.getItems().clear();
        listSearch.removeAll(this.listSearch);
        String[] files = foundFileList.trim().split(" ");
        for (int i = 0; i < files.length; i++) {
            files[i] = files[i].replace("@", " ");
        }
        listSearch.addAll(files);
        searchListView.getItems().addAll(listSearch);
    }

    public void selectItem(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() >= 2 && !searchListView.getSelectionModel().isEmpty()) {
            String item = searchListView.getSelectionModel().getSelectedItem().replace(" ", "@");
            try {
                client.sendCommand("goToFile " + item);
                String listFileLocation = client.readCommand();
                cloudStorageController.goToLocationOfFoundFile(listFileLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

