package org.openjfx.hellofx.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

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
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
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
import javafx.util.StringConverter;

public class MembershipController implements Initializable {

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

    @FXML
    private ComboBox<String> languageCombo;

    private ResourceBundle resources;

    private final AuthService authService = new AuthService();
    private final ClientDAO clientDAO = DaoFactory.clients();
    private final MembershipDAO membershipDAO = DaoFactory.memberships();
    private final SpecializationDAO specializationDAO = DaoFactory.specializations();
    private final VisitDAO visitDAO = DaoFactory.visits();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        setupLanguageSelector();
        if (resultsList != null) {
            resultsList.getItems().addListener((ListChangeListener<HBox>) change -> updateResultsVisibility());
            updateResultsVisibility();
        }
        if (manageUsersButton != null) {
            manageUsersButton.setDisable(!AuthContext.isAdmin());
            manageUsersButton.setVisible(AuthContext.isAdmin());
            manageUsersButton.setManaged(AuthContext.isAdmin());
        }
        if (discountButton != null){
            discountButton.setDisable(!AuthContext.isAdmin());
            discountButton.setVisible(AuthContext.isAdmin());
            discountButton.setManaged(AuthContext.isAdmin());
        }

        boolean isCoach = AuthContext.isCoach();
        if (coachProfileButton != null) {
            coachProfileButton.setVisible(isCoach);
            coachProfileButton.setManaged(isCoach);
        }
        if (availabilityButton != null) {
            availabilityButton.setDisable(!isCoach);
            availabilityButton.setVisible(isCoach);
            availabilityButton.setManaged(isCoach);
        }
        if (visitHistoryButton != null) {
            boolean canViewHistory = AuthContext.isAdmin() || !AuthContext.isCoach();
            visitHistoryButton.setDisable(!canViewHistory);
            visitHistoryButton.setVisible(canViewHistory);
            visitHistoryButton.setManaged(canViewHistory);
        }
        if (titleText != null && isCoach) {
            titleText.setText(get("membership.title.coach"));
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
            showAlert(Alert.AlertType.ERROR, get("membership.coachProfile.notLinked"));
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(get("membership.coachProfile.title"));
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
        grid.addRow(0, new Label(get("membership.coachProfile.currentPassword")), currentPw);
        grid.addRow(1, new Label(get("membership.coachProfile.newPassword")), newPw);
        grid.addRow(2, new Label(get("membership.coachProfile.confirmNew")), confirmPw);
        grid.addRow(3, new Label(get("membership.coachProfile.specializations")), specsField);

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
                showAlert(Alert.AlertType.WARNING, get("membership.coachProfile.password.fill"));
                return;
            }
            if (!npw.equals(cfm)) {
                showAlert(Alert.AlertType.WARNING, get("membership.coachProfile.password.mismatch"));
                return;
            }
            try {
                boolean changed = authService.changePassword(current.id(), curr, npw);
                if (!changed) {
                    showAlert(Alert.AlertType.ERROR, get("membership.coachProfile.password.incorrect"));
                    return;
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, get("membership.coachProfile.password.fail") + ": " + e.getMessage());
                return;
            }
        }

        // update specializations
        try {
            var specs = parseSpecializations(specsField.getText());
            specializationDAO.setSpecializationsForCoach(current.coachId(), specs);
            showAlert(Alert.AlertType.INFORMATION, get("membership.coachProfile.updated"));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, get("membership.coachProfile.specs.fail") + ": " + e.getMessage());
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
        updateResultsVisibility();

        if (query.isEmpty()) {
            searchStatus.setText(get("membership.search.enter"));
            return;
        }

        try {
            List<Client> found = clientDAO.searchClients(query);

            if (found.isEmpty()) {
                searchStatus.setText(get("membership.search.none"));
            } else {
                searchStatus.setText(String.format(get("membership.search.found"), found.size()));
                for (Client c : found) {
                    HBox row = createClientRow(c);
                    resultsList.getItems().add(row);
                }
                updateResultsVisibility();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError(get("error.fetch.clients") + ": " + e.getMessage());
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
        String membershipLabelText = get("membership.type.none");
        String currentType = null;
        Integer remainingVisits = null;
        try {
            currentType = membershipDAO.getCurrentMembershipType(client.id());
            if (currentType != null) {
                membershipLabelText = switch (currentType) {
                    case "Ten" -> get("membership.type.ten");
                    case "Monthly" -> get("membership.type.monthly");
                    case "Weekly" -> get("membership.type.weekly");
                    case "Yearly" -> get("membership.type.yearly");
                    default -> currentType;
                };
                remainingVisits = membershipDAO.getRemainingVisits(client.id());
                if ("Ten".equalsIgnoreCase(currentType) && remainingVisits != null) {
                    membershipLabelText = membershipLabelText + " " + String.format(get("membership.left"), remainingVisits);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        boolean isAdmin = AuthContext.isAdmin();
        Label nameLabel = new Label(client.email() + " (" + client.name() + ") - " + get("membership.label") + ": " + membershipLabelText);
        Button assignButton = new Button(get("membership.assign"));
        assignButton.setOnAction(e -> openAssignMembershipWindow(client));

        Button deleteBtn = new Button(get("membership.delete"));
        boolean hasMembership = currentType != null;
        deleteBtn.setDisable(!isAdmin || !hasMembership);
        deleteBtn.setOnAction(e -> {
            if (!AuthContext.isAdmin()) {
                showAlert(Alert.AlertType.WARNING, get("membership.delete.onlyAdmin"));
                return;
            }

            boolean confirmed = confirmDelete(
                get("membership.delete.title"),
                String.format(get("membership.delete.confirm"), client.name())
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

        Button checkInButton = new Button(get("membership.checkin"));
        boolean isTenMembership = "Ten".equalsIgnoreCase(currentType);
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
                    showAlert(Alert.AlertType.INFORMATION, get("membership.checkin.ok"));
                } else {
                    showAlert(Alert.AlertType.WARNING, get("membership.checkin.invalid"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, get("membership.checkin.fail") + ": " + ex.getMessage());
            }
        });

        HBox row = new HBox(10);
        row.getChildren().addAll(nameLabel, assignButton, deleteBtn, checkInButton);
        return row;
    }

    @FXML
    void onRegisterNewClient(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/register_view.fxml"), App.getBundle());
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(get("window.register"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError(get("membership.open.register.error"));
        }
    }

    @FXML
    void onSearchCoach(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/coach_search_view.fxml"), App.getBundle());
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(get("window.coachSearch"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError(get("membership.open.coachSearch.error"));
        }
    }

    @FXML
    void onOpenWeeklySchedule(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/weekly_schedule.fxml"), App.getBundle());
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(get("window.weeklySchedule"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, get("membership.open.weeklySchedule.error") + ": " + e.getMessage());
        }
    }

    @FXML
    void onBookTrainingSession(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/training_session_booking.fxml"), App.getBundle());
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(get("window.booking"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, get("membership.open.booking.error") + ": " + e.getMessage());
        }
    }

    @FXML
    void onManageUsers(ActionEvent event) {
        if (!AuthContext.isAdmin()) {
            showAlert(Alert.AlertType.WARNING, get("membership.manageUsers.onlyAdmin"));
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/user_management.fxml"), App.getBundle());
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(get("window.userManagement"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, get("membership.open.userManagement.error") + ": " + e.getMessage());
        }
    }

    @FXML
    void onOpenVisitHistory(ActionEvent event) {
        if (!(AuthContext.isAdmin() || !AuthContext.isCoach())) {
            showAlert(Alert.AlertType.WARNING, get("membership.visitHistory.onlyAdminStaff"));
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/visit_history.fxml"), App.getBundle());
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(get("window.visitHistory"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, get("membership.open.visitHistory.error") + ": " + e.getMessage());
        }
    }

    @FXML
    void onAddAvailability(ActionEvent event) {
        if (!AuthContext.isCoach()) {
            showAlert(Alert.AlertType.WARNING, get("membership.availability.onlyCoach"));
            return;
        }
        if (AuthContext.getCurrentUser() == null || AuthContext.getCurrentUser().coachId() == null) {
            showAlert(Alert.AlertType.ERROR, get("membership.availability.noProfile"));
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/add_availability.fxml"), App.getBundle());
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(get("window.addAvailability"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, get("membership.open.availability.error") + ": " + e.getMessage());
        }
    }

    @FXML
    void onLogout(ActionEvent event) {
        authService.logout();
        try {
            App.setRoot("login_view");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, get("membership.logout.fail") + ": " + e.getMessage());
        }
    }

    @FXML
    void onDiscount(ActionEvent event){
        try {
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/openjfx/hellofx/discount_rules.fxml"), App.getBundle());
         Parent root = loader.load();

         Stage dialog = new Stage();
         dialog.setTitle(get("window.discountRules"));
         dialog.initModality(Modality.APPLICATION_MODAL);
         dialog.setScene(new Scene(root));
         dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, get("membership.open.discount.error") + ": " + e.getMessage());
        }
        
    }

    private void openAssignMembershipWindow(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/openjfx/hellofx/assign_view.fxml"), App.getBundle());
            Parent root = loader.load();

            AssignMembershipController controller = loader.getController();
            controller.setClient(client.id(), client.name());
            controller.setOnSaved(() -> onSearchButton(null));

            Stage stage = new Stage();
            stage.setTitle(String.format(get("window.assignMembership"), client.name()));
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError(get("membership.open.assign.error"));
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(get("general.error"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupLanguageSelector() {
        if (languageCombo == null) return;
        languageCombo.getItems().setAll("en", "sk");
        languageCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(String code) {
                return switch (code) {
                    case "sk" -> get("lang.sk");
                    case "en" -> get("lang.en");
                    default -> code;
                };
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        });
        String current = "sk".equals(App.getCurrentLocale().getLanguage()) ? "sk" : "en";
        languageCombo.getSelectionModel().select(current);
        languageCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.equals(oldVal)) return;
            try {
                App.switchLocale(Locale.forLanguageTag(newVal));
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, get("login.error.load"));
            }
        });
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
