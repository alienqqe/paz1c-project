package org.openjfx.hellofx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.openjfx.hellofx.dao.ClientDAO;
import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.dao.CoachAvailabilityDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.TimetableDAO;
import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.entities.Coach;
import org.openjfx.hellofx.model.AvailabilitySlot;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class TrainingSessionBookingController {

    @FXML private TextField titleField;
    @FXML private TextField clientNameField;
    @FXML private TextField coachNameField;
    @FXML private DatePicker datePicker;
    @FXML private Button saveButton;
    @FXML private ListView<AvailabilitySlot> slotsList;
    @FXML private Label statusLabel;
    @FXML private ListView<String> clientSuggestions;
    @FXML private ListView<String> coachSuggestions;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;

    private final ClientDAO clientDAO = DaoFactory.clients();
    private final CoachDAO coachDAO = DaoFactory.coaches();
    private final TimetableDAO timetableDAO = DaoFactory.timetable();
    private final CoachAvailabilityDAO availabilityDAO = DaoFactory.coachAvailability();

    @FXML
    void onSave(ActionEvent event) {
        String title = titleField.getText().trim();
        String clientName = clientNameField.getText().trim();
        String coachName = coachNameField.getText().trim();
        LocalDate date = datePicker.getValue();
        AvailabilitySlot selectedSlot = slotsList.getSelectionModel().getSelectedItem();

        if (title.isEmpty() || clientName.isEmpty() || coachName.isEmpty() || date == null) {
            showAlert(Alert.AlertType.WARNING, "Please fill all fields.");
            return;
        }
        if (selectedSlot == null) {
            showAlert(Alert.AlertType.WARNING, "Please select an availability slot.");
            return;
        }
        if (startTimeField.getText().trim().isEmpty() || endTimeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please set start and end time within the selected slot.");
            return;
        }

        try {
            Long clientId = resolveClientIdByName(clientName);
            Long coachId = resolveCoachIdByName(coachName);
            LocalTime chosenStart = LocalTime.parse(startTimeField.getText().trim());
            LocalTime chosenEnd = LocalTime.parse(endTimeField.getText().trim());
            LocalDateTime start = date.atTime(chosenStart);
            LocalDateTime end = date.atTime(chosenEnd);

            if (end.isBefore(LocalDateTime.now())) {
                showAlert(Alert.AlertType.WARNING, "Cannot book a slot in the past.");
                return;
            }

            if (!end.isAfter(start)) {
                showAlert(Alert.AlertType.WARNING, "End time must be after start time.");
                return;
            }

            // ensure within selected availability window
            if (start.isBefore(selectedSlot.start()) || end.isAfter(selectedSlot.end())) {
                showAlert(Alert.AlertType.WARNING, "Booking must be within the selected availability slot.");
                return;
            }

            // ensure within availability
            boolean withinAvailability = availabilityDAO.isWithinAvailability(coachId, start, end);
            if (!withinAvailability) {
                showAlert(Alert.AlertType.WARNING, "Selected time is outside coach availability.");
                return;
            }

            // prevent double booking
            if (timetableDAO.hasConflictingSession(coachId, start, end)) {
                showAlert(Alert.AlertType.WARNING, "Coach already has a session during this time.");
                return;
            }

            timetableDAO.addTrainingSession(clientId, coachId, start, end, title);
            showAlert(Alert.AlertType.INFORMATION, "Training session booked.");
            closeWindow();
        } catch (IllegalArgumentException iae) {
            showAlert(Alert.AlertType.ERROR, iae.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to save session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onLoadSlots(ActionEvent event) {
        statusLabel.setText("");
        slotsList.getItems().clear();

        String coachName = coachNameField.getText().trim();
        LocalDate date = datePicker.getValue();
        if (coachName.isEmpty() || date == null) {
            statusLabel.setText("Enter coach name and date to load slots.");
            return;
        }

        try {
            availabilityDAO.deleteExpired();
            Long coachId = resolveCoachIdByName(coachName);
            List<AvailabilitySlot> slots = availabilityDAO.getAvailabilityForDate(coachId, date);
            if (slots.isEmpty()) {
                statusLabel.setText("No availability for this date.");
            } else {
                slotsList.getItems().setAll(slots);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load slots: " + e.getMessage());
        } catch (IllegalArgumentException iae) {
            statusLabel.setText(iae.getMessage());
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

    @FXML
    public void initialize() {
        slotsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(AvailabilitySlot item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.start().toLocalTime() + " - " + item.end().toLocalTime() + " (" + item.note() + ")");
                }
            }
        });

        clientSuggestions.setOnMouseClicked(e -> {
            String sel = clientSuggestions.getSelectionModel().getSelectedItem();
            if (sel != null) {
                clientNameField.setText(sel);
                hideClientSuggestions();
            }
        });
        coachSuggestions.setOnMouseClicked(e -> {
            String sel = coachSuggestions.getSelectionModel().getSelectedItem();
            if (sel != null) {
                int idx = sel.indexOf(" (");
                coachNameField.setText(idx > 0 ? sel.substring(0, idx) : sel);
                hideCoachSuggestions();
            }
        });

        clientNameField.textProperty().addListener((obs, oldV, newV) -> updateClientSuggestions(newV));
        coachNameField.textProperty().addListener((obs, oldV, newV) -> updateCoachSuggestions(newV));

        slotsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                startTimeField.setText(newV.start().toLocalTime().toString());
                endTimeField.setText(newV.end().toLocalTime().toString());
            } else {
                startTimeField.clear();
                endTimeField.clear();
            }
        });
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

    private void updateClientSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            hideClientSuggestions();
            return;
        }
        try {
            List<String> names = clientDAO.searchClients(query.trim()).stream()
                .map(Client::name)
                .distinct()
                .collect(Collectors.toList());
            if (names.isEmpty()) {
                hideClientSuggestions();
            } else {
                clientSuggestions.getItems().setAll(names);
                clientSuggestions.setVisible(true);
                clientSuggestions.setManaged(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            hideClientSuggestions();
        }
    }

    private void updateCoachSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            hideCoachSuggestions();
            return;
        }
        try {
            List<Coach> matches = coachDAO.searchCoaches(query.trim());
            List<String> labels = matches.stream()
                .map(c -> {
                    String specs = (c.specializations() != null && !c.specializations().isEmpty())
                        ? " (" + c.specializations().stream().map(s -> s.name()).collect(Collectors.joining(", ")) + ")"
                        : "";
                    return c.name() + specs;
                })
                .distinct()
                .collect(Collectors.toList());
            if (labels.isEmpty()) {
                hideCoachSuggestions();
            } else {
                coachSuggestions.getItems().setAll(labels);
                coachSuggestions.setVisible(true);
                coachSuggestions.setManaged(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            hideCoachSuggestions();
        }
    }

    private void hideClientSuggestions() {
        clientSuggestions.setVisible(false);
        clientSuggestions.setManaged(false);
    }

    private void hideCoachSuggestions() {
        coachSuggestions.setVisible(false);
        coachSuggestions.setManaged(false);
    }
}
