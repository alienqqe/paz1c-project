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
import org.openjfx.hellofx.dao.MembershipDAO;
import org.openjfx.hellofx.dao.VisitDAO;
import org.openjfx.hellofx.entities.Client;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
    private final MembershipDAO membershipDAO = new MembershipDAO();
    private final VisitDAO visitDAO = new VisitDAO();

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
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

    private boolean confirmDelete(String title, String msg) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle(title);
        confirm.setHeaderText(null);

        Optional<ButtonType> res = confirm.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    // Creates one row in the results list: "Client Name  [Assign Button]"
    private HBox createClientRow(Client client) {
        String membershipLabelText = "None";
        try {
            String currentType = membershipDAO.getCurrentMembershipType(client.id());
            if (currentType != null) {
                membershipLabelText = switch (currentType) {
                    case "Ten" -> "10 Visits";
                    case "Monthly" -> "Monthly";
                    case "Weekly" -> "Weekly";
                    case "Yearly" -> "Yearly";
                    default -> currentType;
                };
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        Label nameLabel = new Label(client.email() + " (" + client.name() + ") - Membership: " + membershipLabelText);
        Button assignButton = new Button("Assign");
        assignButton.setOnAction(e -> openAssignMembershipWindow(client));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setDisable("none".equalsIgnoreCase(membershipLabelText));
        deleteBtn.setOnAction(e -> {
            boolean confirmed = confirmDelete(
                "Delete Membership",
                "Are you sure you want to delete membership for " + client.name() + "?"
            );

            if (confirmed) {
                try {
                    membershipDAO.removeByHolderId(client.id());
                    // Refresh results after deletion
                    onSearchButton(null);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button checkInButton = new Button("Check In");
        checkInButton.setDisable("none".equalsIgnoreCase(membershipLabelText));
        checkInButton.setOnAction(e -> {
            try {
                boolean checkedIn = visitDAO.checkInClient(client.id());
                if (checkedIn) {
                    showAlert(Alert.AlertType.INFORMATION, "Check-in successful!");
                } else {
                    showAlert(Alert.AlertType.WARNING, "No valid membership found or already checked in.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Check-in failed: " + ex.getMessage());
            }
        });

        HBox row = new HBox(10);
        row.getChildren().addAll(nameLabel, assignButton, deleteBtn, checkInButton);
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

    @FXML
    void onRegisterNewCoach(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/register_coach_view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Register New Coach");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error");
            alert.setContentText("Failed to open coach registration window.");
            alert.showAndWait();
        }
    }

    @FXML
    void onSearchCoach(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/coach_search_view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Search Coaches");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error");
            alert.setContentText("Failed to open coach search window.");
            alert.showAndWait();
        }
    }

    @FXML
    void onOpenWeeklySchedule(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/weekly_schedule.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Weekly Timetable");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to open weekly timetable: " + e.getMessage());
        }
    }

    @FXML
    void onBookTrainingSession(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/training_session_booking.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Book Training Session");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to open booking window: " + e.getMessage());
        }
    }

    private void openAssignMembershipWindow(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/assign_view.fxml"));
            Parent root = loader.load();

            AssignMembershipController controller = loader.getController();
            controller.setClient(client.id(), client.name());
            controller.setOnSaved(() -> onSearchButton(null));

            Stage stage = new Stage();
            stage.setTitle("Assign Membership - " + client.name());
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
