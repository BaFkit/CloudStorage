package com.bafkit.cloud.storage.client.controllers;

import com.bafkit.cloud.storage.client.controllers.utilities.WindowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;


public class CloudStorageController implements WindowController {

    @FXML
    Button button;

    public void windowCloudStorage() {
        changeWindow(button.getScene(),"authentication");
    }


    public void clickExit(ActionEvent actionEvent) {
        windowCloudStorage();
    }
}
