package org.openjfx.hellofx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.openjfx.hellofx.App;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MembershipController {

    @FXML
    private VBox resultsBox;


    @FXML
    private ListView<HBox> resultsList;

    @FXML
    private Button searchButton;

    @FXML
    private TextField searchField;

    @FXML
    private Label searchStatus;

    // Fake data for demonstration
    private final List<String> allClients = List.of(
            "John Doe",
            "Jane Smith",
            "Michael Johnson",
            "Anna Brown"
    );

    @FXML
    void onSearchButton(ActionEvent event) {
        String query = searchField.getText().trim().toLowerCase();
        resultsList.getItems().clear();

        if (query.isEmpty()) {
            searchStatus.setText("Please enter a name or email.");
            resultsBox.setVisible(false);
            return;
        }

        // Fake "search"
        List<String> found = new ArrayList<>();
        for (String client : allClients) {
            if (client.toLowerCase().contains(query)) {
                found.add(client);
            }
        }

        if (found.isEmpty()) {
            searchStatus.setText("No users found.");
            resultsBox.setVisible(false);
        } else {
            searchStatus.setText("Found " + found.size() + " user(s):");
            resultsBox.setVisible(true);

            for (String name : found) {
                HBox row = createClientRow(name);
                resultsList.getItems().add(row);
            }
        }
    }

    // Creates one row in the results list: "Client Name  [Assign Button]"
    private HBox createClientRow(String clientName) {
        Label nameLabel = new Label(clientName);
        Button assignButton = new Button("Assign");
        assignButton.setOnAction(e -> openAssignMembershipWindow(clientName));

        HBox row = new HBox(10);
        row.getChildren().addAll(nameLabel, assignButton);
        return row;
    }
    @FXML
void onRegisterNewClient(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/register_view.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Register New Client");
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText("Failed to open registration window.");
        alert.showAndWait();
    }
}


    // Opens the assign_membership.fxml in a new window
    private void openAssignMembershipWindow(String clientName) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/assign_view.fxml"));
            Parent root = loader.load();

            AssignMembershipController controller = loader.getController();
            controller.setClientName(clientName);

            Stage stage = new Stage();
            stage.setTitle("Assign Membership - " + clientName);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error opening membership assignment window.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
