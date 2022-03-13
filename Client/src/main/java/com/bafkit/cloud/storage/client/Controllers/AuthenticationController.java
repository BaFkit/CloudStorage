package com.bafkit.cloud.storage.client.Controllers;

import com.bafkit.cloud.storage.client.ClientApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


public class AuthenticationController {

    private ClientApp clientApp;

    public AuthenticationController() {
        clientApp = new ClientApp();
    }

    @FXML
    Button button;

    @FXML
    TextField login;

    @FXML
    PasswordField password;


    public void windowCloudStorage() {
        changeWindow("cloudStorage");
    }

    public void changeWindow(String fxmlName) {
        try {
            Stage stage = (Stage) button.getScene().getWindow();
            stage.close();
            String fxml = "/fxml/" + fxmlName + ".fxml";
            Parent chat = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            stage = new Stage();
            stage.setTitle("Cloud Storage");
            stage.setScene(new Scene(chat, 1024, 768));
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void clickEnter(ActionEvent actionEvent) {
        String command = "auth ".concat(login.getText().trim()).concat(" ").concat(password.getText().trim());
        try {
            clientApp.sendCommand(command);
            command = clientApp.read();
            System.out.println(command);
        } catch (IOException e) {
            e.printStackTrace();
        }

        windowCloudStorage();
    }


}
