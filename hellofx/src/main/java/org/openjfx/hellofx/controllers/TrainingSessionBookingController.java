package org.openjfx.hellofx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.openjfx.hellofx.dao.ClientDAO;
import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.dao.TimetableDAO;
import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.entities.Coach;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class TrainingSessionBookingController {

    @FXML private TextField titleField;
    @FXML private TextField clientNameField;
    @FXML private TextField coachNameField;
    @FXML private DatePicker datePicker;
    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private Button saveButton;

    private final ClientDAO clientDAO = new ClientDAO();
    private final CoachDAO coachDAO = new CoachDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();

    @FXML
    void onSave(ActionEvent event) {
        String title = titleField.getText().trim();
        String clientName = clientNameField.getText().trim();
        String coachName = coachNameField.getText().trim();
        LocalDate date = datePicker.getValue();
        String startText = startField.getText().trim();
        String endText = endField.getText().trim();

        if (title.isEmpty() || clientName.isEmpty() || coachName.isEmpty() || date == null
            || startText.isEmpty() || endText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please fill all fields.");
            return;
        }

        try {
            Long clientId = resolveClientIdByName(clientName);
            Long coachId = resolveCoachIdByName(coachName);
            LocalTime startTime = LocalTime.parse(startText);
            LocalTime endTime = LocalTime.parse(endText);

            LocalDateTime start = date.atTime(startTime);
            LocalDateTime end = date.atTime(endTime);

            if (!end.isAfter(start)) {
                showAlert(Alert.AlertType.WARNING, "End time must be after start time.");
                return;
            }

            timetableDAO.addTrainingSession(clientId, coachId, start, end, title);
            showAlert(Alert.AlertType.INFORMATION, "Training session booked.");
            closeWindow();
        } catch (java.time.format.DateTimeParseException dtpe) {
            showAlert(Alert.AlertType.ERROR, "Time must be in HH:mm format.");
        } catch (IllegalArgumentException iae) {
            showAlert(Alert.AlertType.ERROR, iae.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to save session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onClose(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Long resolveClientIdByName(String name) throws SQLException {
        List<Client> matches = clientDAO.searchClients(name).stream()
            .filter(c -> c.name().equalsIgnoreCase(name))
            .toList();
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("No client found with name: " + name);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Multiple clients found with that name. Please specify uniquely.");
        }
        return matches.get(0).id();
    }

    private Long resolveCoachIdByName(String name) throws SQLException {
        List<Coach> matches = coachDAO.searchCoaches(name).stream()
            .filter(c -> c.name().equalsIgnoreCase(name))
            .toList();
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("No coach found with name: " + name);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Multiple coaches found with that name. Please specify uniquely.");
        }
        return matches.get(0).id();
    }
}
