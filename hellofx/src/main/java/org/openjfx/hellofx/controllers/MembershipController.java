package org.openjfx.hellofx.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.openjfx.hellofx.App;
import org.openjfx.hellofx.dao.ClientDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.MembershipDAO;
import org.openjfx.hellofx.dao.SpecializationDAO;
import org.openjfx.hellofx.dao.VisitDAO;
import org.openjfx.hellofx.entities.Client;
import org.openjfx.hellofx.utils.AuthContext;
import org.openjfx.hellofx.utils.AuthService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    private Button manageUsersButton;

    @FXML
    private Button availabilityButton;

    @FXML
    private Button visitHistoryButton;

    @FXML
    private Button weeklyScheduleButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button coachProfileButton;

    @FXML
    private Button discountButton;

    @FXML
    private HBox searchContainer;

    @FXML
    private HBox actionsRow1;

    @FXML
    private HBox actionsRow2;

    @FXML
    private Label searchStatus;

    @FXML
    private Text titleText;

    private final AuthService authService = new AuthService();
    private final ClientDAO clientDAO = DaoFactory.clients();
    private final MembershipDAO membershipDAO = DaoFactory.memberships();
    private final SpecializationDAO specializationDAO = DaoFactory.specializations();
    private final VisitDAO visitDAO = DaoFactory.visits();

    @FXML
    public void initialize() {
        if (manageUsersButton != null) {
            manageUsersButton.setDisable(!AuthContext.isAdmin());
            manageUsersButton.setVisible(AuthContext.isAdmin());
        }
        if (discountButton != null){
            discountButton.setDisable(!AuthContext.isAdmin());
            discountButton.setVisible(AuthContext.isAdmin());
        }

        boolean isCoach = AuthContext.isCoach();
        if (coachProfileButton != null) {
            coachProfileButton.setVisible(isCoach);
            coachProfileButton.setManaged(isCoach);
        }
        if (availabilityButton != null) {
            availabilityButton.setDisable(!isCoach);
            availabilityButton.setVisible(isCoach);
        }
        if (visitHistoryButton != null) {
            boolean canViewHistory = AuthContext.isAdmin() || !AuthContext.isCoach();
            visitHistoryButton.setDisable(!canViewHistory);
            visitHistoryButton.setVisible(canViewHistory);
        }
        if (titleText != null && isCoach) {
            titleText.setText("Coach dashboard");
        }

        if (isCoach) {
            // hide membership-related UI
            if (searchContainer != null) { searchContainer.setVisible(false); searchContainer.setManaged(false); }
            if (resultsBox != null) { resultsBox.setVisible(false); resultsBox.setManaged(false); }
            if (searchStatus != null) { searchStatus.setVisible(false); searchStatus.setManaged(false); }

            if (actionsRow1 != null) {
                actionsRow1.getChildren().forEach(node -> {
                    if (node == weeklyScheduleButton) {
                        node.setVisible(true);
                        node.setDisable(false);
                        node.setManaged(true);
                    } else {
                        node.setVisible(false);
                        node.setManaged(false);
                    }
                });
            }
            if (actionsRow2 != null) {
                actionsRow2.getChildren().forEach(node -> {
                    if (node == availabilityButton || node == logoutButton || node == coachProfileButton) {
                        node.setVisible(true);
                        node.setDisable(false);
                        node.setManaged(true);
                    } else {
                        node.setVisible(false);
                        node.setManaged(false);
                    }
                });
            }
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void onCoachProfile() {
        if (!AuthContext.isCoach()) return;
        var current = AuthContext.getCurrentUser();
        if (current == null || current.coachId() == null) {
            showAlert(Alert.AlertType.ERROR, "Coach profile not linked.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Coach Profile");
        dialog.initModality(Modality.APPLICATION_MODAL);

        PasswordField currentPw = new PasswordField();
        PasswordField newPw = new PasswordField();
        PasswordField confirmPw = new PasswordField();
        TextField specsField = new TextField();

        try {
            var specs = specializationDAO.getSpecializationsForCoach(current.coachId());
            if (specs != null && !specs.isEmpty()) {
                String joined = specs.stream().map(s -> s.name()).collect(java.util.stream.Collectors.joining(", "));
                specsField.setText(joined);
            }
        } catch (SQLException e) {
            // ignore and leave blank
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Current password:"), currentPw);
        grid.addRow(1, new Label("New password:"), newPw);
        grid.addRow(2, new Label("Confirm new:"), confirmPw);
        grid.addRow(3, new Label("Specializations (comma-separated):"), specsField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> btn);
        var result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        // change password
        String curr = currentPw.getText();
        String npw = newPw.getText();
        String cfm = confirmPw.getText();
        if (!curr.isBlank() || !npw.isBlank() || !cfm.isBlank()) {
            if (curr.isBlank() || npw.isBlank() || cfm.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Fill all password fields or leave all empty.");
                return;
            }
            if (!npw.equals(cfm)) {
                showAlert(Alert.AlertType.WARNING, "New passwords do not match.");
                return;
            }
            try {
                boolean changed = authService.changePassword(current.id(), curr, npw);
                if (!changed) {
                    showAlert(Alert.AlertType.ERROR, "Current password incorrect.");
                    return;
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Failed to change password: " + e.getMessage());
                return;
            }
        }

        // update specializations
        try {
            var specs = parseSpecializations(specsField.getText());
            specializationDAO.setSpecializationsForCoach(current.coachId(), specs);
            showAlert(Alert.AlertType.INFORMATION, "Profile updated.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to update specializations: " + e.getMessage());
        }
    }

    private java.util.Set<String> parseSpecializations(String raw) {
        if (raw == null || raw.isBlank()) return java.util.Set.of();
        java.util.Set<String> specs = new java.util.HashSet<>();
        String[] parts = raw.split(",");
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (!t.isEmpty()) specs.add(t);
        }
        return specs;
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
        Integer remainingVisits = null;
        try {
            String currentType = membershipDAO.getCurrentMembershipType(client.id());
            if (currentType != null) {
                boolean currentHasLeft = currentType.toLowerCase().contains("left");
                membershipLabelText = switch (currentType) {
                    case "Ten" -> "10 Visits";
                    case "Monthly" -> "Monthly";
                    case "Weekly" -> "Weekly";
                    case "Yearly" -> "Yearly";
                    default -> currentType;
                };
                remainingVisits = membershipDAO.getRemainingVisits(client.id());
                if (!currentHasLeft && membershipLabelText.toLowerCase().contains("10") && remainingVisits != null) {
                    membershipLabelText = membershipLabelText + " (" + remainingVisits + " left)";
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        boolean isAdmin = AuthContext.isAdmin();
        Label nameLabel = new Label(client.email() + " (" + client.name() + ") - Membership: " + membershipLabelText);
        Button assignButton = new Button("Assign");
        assignButton.setOnAction(e -> openAssignMembershipWindow(client));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setDisable(!isAdmin || "none".equalsIgnoreCase(membershipLabelText));
        deleteBtn.setOnAction(e -> {
            if (!AuthContext.isAdmin()) {
                showAlert(Alert.AlertType.WARNING, "Only admins can delete memberships.");
                return;
            }

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
        boolean isTenMembership = membershipLabelText.toLowerCase().contains("10");
        boolean hasMembership = !"none".equalsIgnoreCase(membershipLabelText);
        boolean tenExhausted = isTenMembership && remainingVisits != null && remainingVisits <= 0;
        boolean hasActiveMembership = true;
        try {
            hasActiveMembership = membershipDAO.hasActiveMembership(client.id());
        } catch (SQLException ex) {
            ex.printStackTrace();
            hasActiveMembership = false;
        }
        checkInButton.setDisable(!hasMembership || tenExhausted || !hasActiveMembership);
        checkInButton.setOnAction(e -> {
            try {
                boolean checkedIn = visitDAO.checkInClient(client.id());
                if (checkedIn) {
                    // Refresh the row to reflect updated remaining visits / status
                    HBox refreshed = createClientRow(client);
                    int idx = resultsList.getItems().indexOf(checkInButton.getParent());
                    if (idx >= 0) {
                        resultsList.getItems().set(idx, refreshed);
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Check-in successful!");
                } else {
                    showAlert(Alert.AlertType.WARNING, "No valid membership found.");
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

    @FXML
    void onManageUsers(ActionEvent event) {
        if (!AuthContext.isAdmin()) {
            showAlert(Alert.AlertType.WARNING, "Only admins can manage users.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/user_management.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("User Management");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to open user management: " + e.getMessage());
        }
    }

    @FXML
    void onOpenVisitHistory(ActionEvent event) {
        if (!(AuthContext.isAdmin() || !AuthContext.isCoach())) {
            showAlert(Alert.AlertType.WARNING, "Only admins or staff can view visit history.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/visit_history.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Visit History");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to open visit history: " + e.getMessage());
        }
    }

    @FXML
    void onAddAvailability(ActionEvent event) {
        if (!AuthContext.isCoach()) {
            showAlert(Alert.AlertType.WARNING, "Only coaches can add availability.");
            return;
        }
        if (AuthContext.getCurrentUser() == null || AuthContext.getCurrentUser().coachId() == null) {
            showAlert(Alert.AlertType.ERROR, "Your account is not linked to a coach profile.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/add_availability.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Availability");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to open availability window: " + e.getMessage());
        }
    }

    @FXML
    void onLogout(ActionEvent event) {
        authService.logout();
        try {
            App.setRoot("login_view");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to logout: " + e.getMessage());
        }
    }

    @FXML
    void onDiscount(ActionEvent event){
        try {
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/openjfx/hellofx/discount_rules.fxml"));
         Parent root = loader.load();

         Stage dialog = new Stage();
         dialog.setTitle("Discount rules");
         dialog.initModality(Modality.APPLICATION_MODAL);
         dialog.setScene(new Scene(root));
         dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to open discount window: " + e.getMessage());
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
