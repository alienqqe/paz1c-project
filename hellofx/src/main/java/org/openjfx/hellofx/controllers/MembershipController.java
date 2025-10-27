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
import org.openjfx.hellofx.dao.ClientDAO;
import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.utils.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private final ClientDAO clientDAO = new ClientDAO();


    private List<String> searchClientsInDatabase(String query) {
        List<String> clients = new ArrayList<>();

        String sql = """
            SELECT name, email FROM clients
            WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + query + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    clients.add(name + " (" + email + ")");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error fetching clients: " + e.getMessage());
        }

        return clients;
    }

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
            List<Client> found = clientDAO.searchClients(query);

            if (found.isEmpty()) {
                searchStatus.setText("No users found.");
                resultsBox.setVisible(false);
            } else {
                searchStatus.setText("Found " + found.size() + " user(s):");
                resultsBox.setVisible(true);
                for (Client c : found) {
                    HBox row = createClientRow(c);
                    resultsList.getItems().add(row);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error fetching clients: " + e.getMessage());
        }
    }

    // Creates one row in the results list: "Client Name  [Assign Button]"
      private HBox createClientRow(Client client) {
        Label nameLabel = new Label(client.email() + " (" + client.phoneNumber() + ")");
        Button assignButton = new Button("Assign");
        assignButton.setOnAction(e -> openAssignMembershipWindow(client.name()));

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
