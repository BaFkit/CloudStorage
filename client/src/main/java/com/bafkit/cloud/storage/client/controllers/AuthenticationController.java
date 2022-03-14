package com.bafkit.cloud.storage.client.controllers;

import com.bafkit.cloud.storage.client.Client;
import com.bafkit.cloud.storage.client.controllers.utilities.WindowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

import java.io.IOException;



public class AuthenticationController implements WindowController {

    private final Client client;

    public AuthenticationController() {
        client = Client.getClient();
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
        if(login.getText().trim().isEmpty() || password.getText().trim().isEmpty()) {
            return;
        }
        String command = "auth ".concat(login.getText().trim()).concat(" ").concat(password.getText().trim());
        try {
            client.sendCommand(command);
            command = client.readCommand();
            if (command.equals("success")) {
                windowCloudStorage();
            }else {
                login.clear();
                password.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
