package org.openjfx.hellofx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.entities.Coach;

import java.sql.SQLException;
import java.util.List;

public class CoachSearchController {

    @FXML private VBox resultsBox;
    @FXML private ListView<HBox> resultsList;
    @FXML private TextField searchField;
    @FXML private Label searchStatus;

    private final CoachDAO coachDAO = new CoachDAO();

    @FXML
    void onSearchButton(ActionEvent event) {
        String query = searchField.getText().trim();
        resultsList.getItems().clear();

        if (query.isEmpty()) {
            searchStatus.setText("Please enter a name or email.");
            resultsBox.setVisible(false);
            return;
        }

        try {
            List<Coach> found = coachDAO.searchCoaches(query);
            if (found.isEmpty()) {
                searchStatus.setText("No coaches found.");
                resultsBox.setVisible(false);
            } else {
                searchStatus.setText("Found " + found.size() + " coach(es):");
                resultsBox.setVisible(true);
                for (Coach coach : found) {
                    HBox row = createCoachRow(coach);
                    resultsList.getItems().add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error fetching coaches: " + e.getMessage());
        }
    }

    private HBox createCoachRow(Coach coach) {
        String email = coach.email() == null || coach.email().isBlank() ? "No email" : coach.email();
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
}
