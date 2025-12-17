

package org.openjfx.hellofx.controllers;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.openjfx.hellofx.dao.ClientDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.entities.Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RegisterController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label statusLabel;

    private final ClientDAO clientDAO = DaoFactory.clients();
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        if (statusLabel != null) {
            statusLabel.setText("");
            statusLabel.setVisible(false);
        }
    }

    @FXML
    void onRegisterButton(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, get("register.error.allFields"));
            return;
        }
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, get("register.error.email"));
            return;
        }

        Client client = new Client(null, name, email, phone);

        try {
            clientDAO.addClient(client);
            showAlert(Alert.AlertType.INFORMATION, get("register.success"));
            clearFields();
        } catch (SQLException e) {  
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, get("register.error.fail") + ": " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setVisible(true);
        }
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
    }

    private boolean isValidEmail(String email) {
        // regex was taken from https://www.baeldung.com/java-email-validation-regex
        return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    }

    private String get(String key) {
        return resources != null && resources.containsKey(key) ? resources.getString(key) : key;
    }
}
