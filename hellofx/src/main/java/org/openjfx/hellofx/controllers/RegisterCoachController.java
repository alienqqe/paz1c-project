package org.openjfx.hellofx.controllers;

import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.entities.Coach;
import org.openjfx.hellofx.utils.AuthService;

public class RegisterCoachController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label statusLabel;

    private final CoachDAO coachDAO = new CoachDAO();
    private final AuthService authService = new AuthService();

    @FXML
    void onRegisterButton(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Name, email, and phone are required.");
            return;
        }
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Enter a valid email address.");
            return;
        }
        if (!phone.matches("\\d{6,8}")) {
            showAlert(Alert.AlertType.WARNING, "Phone must be 6, 7, or 8 digits.");
            return;
        }

        Coach coach = new Coach(null, name, email, phone, null, null);

        try {
            Long coachId = coachDAO.addCoach(coach);
            if (coachId == null) {
                showAlert(Alert.AlertType.ERROR, "Failed to create coach.");
                return;
            }

            String normalizedName = name.trim().replaceAll("\\s+", "_").toLowerCase();
            String username = normalizedName + coachId;
            String initialPassword = phone; // initial password set to phone; coach should change it later

            if (coachId != null) {
                authService.createUser(username, initialPassword, "COACH", coachId);
            }

            showAlert(Alert.AlertType.INFORMATION,
                "Coach registered. Login with username: " + username + " and initial password: " + initialPassword);
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

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.!#$%&'*+/=?`{|}~-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
