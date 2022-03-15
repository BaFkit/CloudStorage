package com.bafkit.cloud.storage.client.controllers.utilities;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public interface WindowController {

    default void changeWindow(Scene scene, String fxmlName) {
        try {
            Stage stage = (Stage) scene.getWindow();
            stage.close();
            String fxml = "/fxml/" + fxmlName + ".fxml";
            Parent chat = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            stage = new Stage();
            stage.setTitle("fxmlName");
            stage.setScene(new Scene(chat));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
