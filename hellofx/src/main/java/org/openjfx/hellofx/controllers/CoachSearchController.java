package org.openjfx.hellofx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.entities.Coach;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class CoachSearchController implements Initializable {

    @FXML private VBox resultsBox;
    @FXML private ListView<HBox> resultsList;
    @FXML private TextField searchField;
    @FXML private Label searchStatus;

    private final CoachDAO coachDAO = DaoFactory.coaches();
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        if (resultsList != null) {
            resultsList.getItems().addListener((ListChangeListener<HBox>) change -> updateResultsVisibility());
            updateResultsVisibility();
        }
    }

    @FXML
    void onSearchButton(ActionEvent event) {
        String query = searchField.getText().trim();
        resultsList.getItems().clear();
        updateResultsVisibility();

        if (query.isEmpty()) {
            searchStatus.setText(get("coachSearch.status.enter"));
            return;
        }

        try {
            List<Coach> found = coachDAO.searchCoaches(query);
            if (found.isEmpty()) {
                searchStatus.setText(get("coachSearch.status.none"));
            } else {
                searchStatus.setText(String.format(get("coachSearch.status.found"), found.size()));
                for (Coach coach : found) {
                    HBox row = createCoachRow(coach);
                    resultsList.getItems().add(row);
                }
                updateResultsVisibility();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, get("coachSearch.error.fetch") + ": " + e.getMessage());
        }
    }

    private HBox createCoachRow(Coach coach) {
        String email = coach.email() == null || coach.email().isBlank() ? get("coachSearch.noEmail") : coach.email();
        Label label = new Label(coach.name() + " | " + email + " | " + coach.phoneNumber());
        HBox row = new HBox(10);
        row.getChildren().add(label);
        return row;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String get(String key) {
        return resources != null && resources.containsKey(key) ? resources.getString(key) : key;
    }

    private void updateResultsVisibility() {
        if (resultsBox == null || resultsList == null) return;
        boolean hasItems = !resultsList.getItems().isEmpty();
        resultsBox.setVisible(hasItems);
        resultsBox.setManaged(hasItems);
        resultsList.setVisible(hasItems);
        resultsList.setManaged(hasItems);
    }
}
