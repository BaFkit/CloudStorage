package com.bafkit.cloud.storage.client.controllers;

import com.bafkit.cloud.storage.client.ClientApp;
import com.bafkit.cloud.storage.client.controllers.utilities.WindowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

import java.io.IOException;



public class AuthenticationController implements WindowController {

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
        changeWindow(button.getScene(), "cloudStorage");
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
