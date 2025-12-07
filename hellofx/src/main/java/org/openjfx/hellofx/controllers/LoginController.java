package org.openjfx.hellofx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.openjfx.hellofx.App;
import org.openjfx.hellofx.utils.AuthService;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private final AuthService authService = new AuthService();

    @FXML
    void onLogin(ActionEvent event) {
        statusLabel.setText("");
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Enter username and password.");
            return;
        }

        try {
            boolean ok = authService.login(username, password);
            if (ok) {
                App.setRoot("membership_view");
            } else {
                statusLabel.setText("Invalid credentials.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Login failed: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Unable to load main view.");
        }
    }
}
