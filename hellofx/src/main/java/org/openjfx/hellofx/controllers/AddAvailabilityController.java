package org.openjfx.hellofx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.openjfx.hellofx.dao.CoachAvailabilityDAO;
import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.utils.AuthContext;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddAvailabilityController {

    @FXML private DatePicker datePicker;
    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private TextField noteField;
    @FXML private Button saveButton;

    private final CoachAvailabilityDAO availabilityDAO = DaoFactory.coachAvailability();
    private final CoachDAO coachDAO = DaoFactory.coaches();

    @FXML
    void onSave(ActionEvent event) {
        if (!AuthContext.isCoach()) {
            showAlert(Alert.AlertType.WARNING, "Only coaches can add availability.");
            return;
        }

        Long coachId = AuthContext.getCurrentUser() != null ? AuthContext.getCurrentUser().coachId() : null;
        if (coachId == null && AuthContext.getCurrentUser() != null) {
            try {
                coachId = coachDAO.findCoachIdForUser(AuthContext.getCurrentUser().username());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (coachId == null) {
            showAlert(Alert.AlertType.ERROR, "Your account is not linked to a coach profile.");
            return;
        }

        LocalDate date = datePicker.getValue();
        String startText = startField.getText().trim();
        String endText = endField.getText().trim();
        String note = noteField.getText().trim();

        if (date == null || startText.isEmpty() || endText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please fill date, start and end time.");
            return;
        }

        try {
            LocalTime startTime = LocalTime.parse(startText);
            LocalTime endTime = LocalTime.parse(endText);
            LocalDateTime start = date.atTime(startTime);
            LocalDateTime end = date.atTime(endTime);

            if (!end.isAfter(start)) {
                showAlert(Alert.AlertType.WARNING, "End time must be after start time.");
                return;
            }

            availabilityDAO.addAvailability(coachId, start, end, note.isEmpty() ? "Available" : note);
            showAlert(Alert.AlertType.INFORMATION, "Availability saved.");
            closeWindow();
        } catch (java.time.format.DateTimeParseException dtpe) {
            showAlert(Alert.AlertType.ERROR, "Time must be in HH:mm format.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to save availability: " + e.getMessage());
        }
    }

    @FXML
    void onCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
