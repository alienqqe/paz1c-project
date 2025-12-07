package org.openjfx.hellofx.controllers;

import java.sql.*;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.openjfx.hellofx.dao.ClientDAO;
import org.openjfx.hellofx.entities.Client;


public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label statusLabel;

      private final ClientDAO clientDAO = new ClientDAO();


    @FXML
    void onRegisterButton(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "All fields are required.");
            return;
        }

        Client client = new Client(null, name, email, phone);
        
        try {
            clientDAO.addClient(client);
            showAlert(Alert.AlertType.INFORMATION, "Client registered successfully!");
            clearFields();
        } catch (SQLException e) {  
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error registering client: " + e.getMessage());
        }
    }
        private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
    }
}
