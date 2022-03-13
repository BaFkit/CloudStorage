package com.bafkit.cloud.storage.client.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CloudStorageController {

    @FXML
    private javafx.scene.control.Button button;

    public void windowCloudStorage() {
        changeWindow("authentication");
    }

    public void changeWindow(String fxmlName) {
        try {
            Stage stage = (Stage) button.getScene().getWindow();
            stage.close();
            String fxml = "/fxml/" + fxmlName + ".fxml";
            Parent chat = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            stage = new Stage();
            stage.setTitle("Authentication");
            stage.setScene(new Scene(chat, 300, 380));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickExit(ActionEvent actionEvent) {
        windowCloudStorage();
    }
}
