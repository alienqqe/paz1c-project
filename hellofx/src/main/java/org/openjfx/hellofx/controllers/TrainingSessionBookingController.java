package org.openjfx.hellofx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TrainingSessionBookingController implements Initializable {

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
    private ResourceBundle resources;

    @FXML
    void onSave(ActionEvent event) {
        String title = titleField.getText().trim();
        String clientName = clientNameField.getText().trim();
        String coachName = coachNameField.getText().trim();
        LocalDate date = datePicker.getValue();
        AvailabilitySlot selectedSlot = slotsList.getSelectionModel().getSelectedItem();

        if (title.isEmpty() || clientName.isEmpty() || coachName.isEmpty() || date == null) {
            showAlert(Alert.AlertType.WARNING, get("booking.error.fill"));
            return;
        }
        if (selectedSlot == null) {
            showAlert(Alert.AlertType.WARNING, get("booking.error.selectSlot"));
            return;
        }
        if (startTimeField.getText().trim().isEmpty() || endTimeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, get("booking.error.setTimes"));
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
                showAlert(Alert.AlertType.WARNING, get("booking.error.past"));
                return;
            }

            if (!end.isAfter(start)) {
                showAlert(Alert.AlertType.WARNING, get("booking.error.endAfter"));
                return;
            }

            // ensure within selected availability window
            if (start.isBefore(selectedSlot.start()) || end.isAfter(selectedSlot.end())) {
                showAlert(Alert.AlertType.WARNING, get("booking.error.within"));
                return;
            }

            // ensure within availability
            boolean withinAvailability = availabilityDAO.isWithinAvailability(coachId, start, end);
            if (!withinAvailability) {
                showAlert(Alert.AlertType.WARNING, get("booking.error.outside"));
                return;
            }

            // prevent double booking
            if (timetableDAO.hasConflictingSession(coachId, start, end)) {
                showAlert(Alert.AlertType.WARNING, get("booking.error.conflict"));
                return;
            }

            timetableDAO.addTrainingSession(clientId, coachId, start, end, title);
            showAlert(Alert.AlertType.INFORMATION, get("booking.success"));
            closeWindow();
        } catch (IllegalArgumentException iae) {
            showAlert(Alert.AlertType.ERROR, iae.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, get("booking.error.save") + ": " + e.getMessage());
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
            statusLabel.setText(get("booking.status.enter"));
            return;
        }

        try {
            availabilityDAO.deleteExpired();
            Long coachId = resolveCoachIdByName(coachName);
            List<AvailabilitySlot> slots = availabilityDAO.getAvailabilityForDate(coachId, date);
            if (slots.isEmpty()) {
                statusLabel.setText(get("booking.status.none"));
            } else {
                slotsList.getItems().setAll(slots);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText(get("booking.status.loadFail") + ": " + e.getMessage());
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
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
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
            throw new IllegalArgumentException(String.format(get("booking.error.noClient"), name));
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException(get("booking.error.multipleClient"));
        }
        return matches.get(0).id();
    }

    private Long resolveCoachIdByName(String name) throws SQLException {
        List<Coach> matches = coachDAO.searchCoaches(name).stream()
            .filter(c -> c.name().equalsIgnoreCase(name))
            .toList();
        if (matches.isEmpty()) {
            throw new IllegalArgumentException(String.format(get("booking.error.noCoach"), name));
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException(get("booking.error.multipleCoach"));
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

    private String get(String key) {
        return resources != null && resources.containsKey(key) ? resources.getString(key) : key;
    }
}
