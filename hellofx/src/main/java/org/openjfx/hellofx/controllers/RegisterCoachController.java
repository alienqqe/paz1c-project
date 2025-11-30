package org.openjfx.hellofx.controllers;

import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.entities.Coach;

public class RegisterCoachController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label statusLabel;

    private final CoachDAO coachDAO = new CoachDAO();

    @FXML
    void onRegisterButton(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Name and phone are required.");
            return;
        }

        String emailValue = email.isEmpty() ? null : email;
        Coach coach = new Coach(null, name, emailValue, phone, null, null);

        try {
            coachDAO.addCoach(coach);
            showAlert(Alert.AlertType.INFORMATION, "Coach registered successfully!");
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error registering coach: " + e.getMessage());
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
