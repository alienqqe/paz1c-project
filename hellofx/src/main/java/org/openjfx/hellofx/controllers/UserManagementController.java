package org.openjfx.hellofx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.openjfx.hellofx.entities.User;
import org.openjfx.hellofx.entities.Coach;
import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.utils.AuthContext;
import org.openjfx.hellofx.utils.AuthService;

import java.sql.SQLException;
import java.util.Arrays;

public class UserManagementController {

    @FXML
    private TextField newUsernameField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField newPasswordConfirmField;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label passwordLabel;

    @FXML
    private Label confirmLabel;

    @FXML
    private ChoiceBox<String> roleChoice;

    @FXML
    private TextField coachNameField;

    @FXML
    private TextField coachEmailField;

    @FXML
    private TextField coachPhoneField;

    @FXML
    private Label coachNameLabel;

    @FXML
    private Label coachEmailLabel;

    @FXML
    private Label coachPhoneLabel;

    @FXML
    private Label coachNoteLabel;

    @FXML
    private Label createStatus;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newSelfPasswordField;

    @FXML
    private PasswordField confirmSelfPasswordField;

    @FXML
    private Label changeStatus;

    private final AuthService authService = new AuthService();
    private final CoachDAO coachDAO = new CoachDAO();

    @FXML
    public void initialize() {
        roleChoice.getItems().addAll(Arrays.asList("STAFF", "ADMIN", "COACH"));
        roleChoice.getSelectionModel().select("STAFF");
        roleChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> toggleCoachFields("COACH".equalsIgnoreCase(newV)));

        // default: enable username/password fields
        toggleCoachFields(false);
    }

    @FXML
    void onCreateUser() {
        createStatus.setStyle("-fx-text-fill: red;");
        createStatus.setText("");

        if (!AuthContext.isAdmin()) {
            createStatus.setText("Only admins can create users.");
            return;
        }

        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText();
        String confirm = newPasswordConfirmField.getText();
        String role = roleChoice.getSelectionModel().getSelectedItem();
        boolean isCoach = "COACH".equalsIgnoreCase(role);

        if (!isCoach) {
            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                createStatus.setText("Fill all fields.");
                return;
            }
            if (!password.equals(confirm)) {
                createStatus.setText("Passwords do not match.");
                return;
            }
            if (role == null || role.isEmpty()) {
                createStatus.setText("Pick a role.");
                return;
            }
        } else {
            // coach flow will auto-assign username/password below
            username = username; // keep for clarity; will be overwritten
        }

        Long coachId = null;
        if ("COACH".equalsIgnoreCase(role)) {
            String coachName = coachNameField.getText().trim();
            String coachPhone = coachPhoneField.getText().trim();
            String coachEmail = coachEmailField.getText().trim();
            if (coachName.isEmpty() || coachPhone.isEmpty()) {
                createStatus.setText("Coach name and phone are required.");
                return;
            }
            try {
                Coach coach = new Coach(null, coachName, coachEmail.isBlank() ? null : coachEmail, coachPhone, null, null);
                coachId = coachDAO.addCoach(coach);
                // auto-assign username/password for coach
                username = !coachEmail.isBlank() ? coachEmail : "coach" + coachId;
                password = coachPhone;
                confirm = coachPhone;
            } catch (SQLException e) {
                e.printStackTrace();
                createStatus.setText("Failed to create coach: " + e.getMessage());
                return;
            }
        }

        try {
            authService.createUser(username, password, role, coachId);
            createStatus.setStyle("-fx-text-fill: green;");
            createStatus.setText("User created.");
            newUsernameField.clear();
            newPasswordField.clear();
            newPasswordConfirmField.clear();
            if (coachNameField != null) coachNameField.clear();
            if (coachEmailField != null) coachEmailField.clear();
            if (coachPhoneField != null) coachPhoneField.clear();
            roleChoice.getSelectionModel().select("STAFF");
            toggleCoachFields(false);
        } catch (SQLException e) {
            e.printStackTrace();
            createStatus.setStyle("-fx-text-fill: red;");
            createStatus.setText("Failed: " + e.getMessage());
        }
    }

    @FXML
    void onChangePassword() {
        changeStatus.setStyle("-fx-text-fill: red;");
        changeStatus.setText("");
        User current = AuthContext.getCurrentUser();
        if (current == null) {
            changeStatus.setText("Not logged in.");
            return;
        }

        String currentPw = currentPasswordField.getText();
        String newPw = newSelfPasswordField.getText();
        String confirm = confirmSelfPasswordField.getText();

        if (currentPw.isEmpty() || newPw.isEmpty() || confirm.isEmpty()) {
            changeStatus.setText("Fill all fields.");
            return;
        }
        if (!newPw.equals(confirm)) {
            changeStatus.setText("New passwords do not match.");
            return;
        }

        try {
            boolean changed = authService.changePassword(current.id(), currentPw, newPw);
            if (changed) {
                changeStatus.setStyle("-fx-text-fill: green;");
                changeStatus.setText("Password updated.");
                currentPasswordField.clear();
                newSelfPasswordField.clear();
                confirmSelfPasswordField.clear();
            } else {
                changeStatus.setStyle("-fx-text-fill: red;");
                changeStatus.setText("Current password incorrect.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            changeStatus.setStyle("-fx-text-fill: red;");
            changeStatus.setText("Failed: " + e.getMessage());
        }
    }

    private void toggleCoachFields(boolean show) {
        // toggle extra coach-specific fields
        if (coachNameField != null) {
            coachNameField.setVisible(show);
            coachNameField.setManaged(show);
        }
        if (coachEmailField != null) {
            coachEmailField.setVisible(show);
            coachEmailField.setManaged(show);
        }
        if (coachPhoneField != null) {
            coachPhoneField.setVisible(show);
            coachPhoneField.setManaged(show);
        }
        if (coachNameLabel != null) {
            coachNameLabel.setVisible(show);
            coachNameLabel.setManaged(show);
        }
        if (coachEmailLabel != null) {
            coachEmailLabel.setVisible(show);
            coachEmailLabel.setManaged(show);
        }
        if (coachPhoneLabel != null) {
            coachPhoneLabel.setVisible(show);
            coachPhoneLabel.setManaged(show);
        }
        if (coachNoteLabel != null) {
            coachNoteLabel.setVisible(show);
            coachNoteLabel.setManaged(show);
        }

        // toggle base username/password fields: hide/disable for coach to avoid confusion
        if (newUsernameField != null) {
            newUsernameField.setVisible(!show);
            newUsernameField.setManaged(!show);
        }
        if (usernameLabel != null) {
            usernameLabel.setVisible(!show);
            usernameLabel.setManaged(!show);
        }
        if (newPasswordField != null) {
            newPasswordField.setVisible(!show);
            newPasswordField.setManaged(!show);
        }
        if (passwordLabel != null) {
            passwordLabel.setVisible(!show);
            passwordLabel.setManaged(!show);
        }
        if (newPasswordConfirmField != null) {
            newPasswordConfirmField.setVisible(!show);
            newPasswordConfirmField.setManaged(!show);
        }
        if (confirmLabel != null) {
            confirmLabel.setVisible(!show);
            confirmLabel.setManaged(!show);
        }
    }
}
