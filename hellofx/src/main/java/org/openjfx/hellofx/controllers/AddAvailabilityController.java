package org.openjfx.hellofx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.openjfx.hellofx.dao.CoachAvailabilityDAO;
import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.utils.AuthContext;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class AddAvailabilityController implements Initializable {

    @FXML private DatePicker datePicker;
    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private TextField noteField;
    @FXML private Button saveButton;

    private final CoachAvailabilityDAO availabilityDAO = DaoFactory.coachAvailability();
    private final CoachDAO coachDAO = DaoFactory.coaches();
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        if (datePicker != null) {
            datePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        return;
                    }
                    if (item.isBefore(LocalDate.now())) {
                        setDisable(true);
                    }
                }
            });
        }
    }

    @FXML
    void onSave(ActionEvent event) {
        if (!AuthContext.isCoach()) {
            showAlert(Alert.AlertType.WARNING, get("availability.error.onlyCoach"));
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
            showAlert(Alert.AlertType.ERROR, get("availability.error.noCoachProfile"));
            return;
        }

        LocalDate date = datePicker.getValue();
        String startText = startField.getText().trim();
        String endText = endField.getText().trim();
        String note = noteField.getText().trim();

        if (date == null || startText.isEmpty() || endText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, get("availability.error.fill"));
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, get("availability.error.pastDate"));
            return;
        }

        try {
            LocalTime startTime = LocalTime.parse(startText);
            LocalTime endTime = LocalTime.parse(endText);
            LocalDateTime start = date.atTime(startTime);
            LocalDateTime end = date.atTime(endTime);

            if (start.isBefore(LocalDateTime.now())) {
                showAlert(Alert.AlertType.WARNING, get("availability.error.pastDate"));
                return;
            }
            if (!end.isAfter(start)) {
                showAlert(Alert.AlertType.WARNING, get("availability.error.endAfter"));
                return;
            }

            availabilityDAO.addAvailability(coachId, start, end, note.isEmpty() ? get("availability.defaultNote") : note);
            showAlert(Alert.AlertType.INFORMATION, get("availability.success.saved"));
            closeWindow();
        } catch (java.time.format.DateTimeParseException dtpe) {
            showAlert(Alert.AlertType.ERROR, get("availability.error.timeFormat"));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, get("availability.error.save") + ": " + e.getMessage());
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

    private String get(String key) {
        return resources != null && resources.containsKey(key) ? resources.getString(key) : key;
    }
}
