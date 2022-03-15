package com.bafkit.cloud.storage.client.controllers;

import com.bafkit.cloud.storage.client.Client;
import com.bafkit.cloud.storage.client.controllers.utilities.WindowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegistrationController implements WindowController {

    private Client client;

    public RegistrationController() {

    }

    @FXML
    Button buttonSubmit;

    @FXML
    TextField login;

    @FXML
    PasswordField password;

    @FXML
    PasswordField repeat_password;

    @FXML
    Hyperlink back;

    public void clickSubmit(ActionEvent actionEvent) {
        if(login.getText().trim().isEmpty() || password.getText().trim().isEmpty() || repeat_password.getText().trim().isEmpty() ) {
            return;
        }
        if(!password.getText().trim().equals(repeat_password.getText().trim())){
            return;
        }
        client = Client.getClient();
        String command = "reg ".concat(login.getText().trim()).concat(" ").concat(password.getText().trim());
        try {
            client.sendCommand(command);
            command = client.readCommand();
            if (command.equals("busy")) {
                System.out.println("Login busy");
                login.clear();
                password.clear();
                repeat_password.clear();
                return;
            }
            if (command.equals("success")) {
                changeWindow(buttonSubmit.getScene(), "authentication");
            }else {
                login.clear();
                password.clear();
                repeat_password.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void back(ActionEvent actionEvent) {
        changeWindow(buttonSubmit.getScene(), "authentication");
    }
}
