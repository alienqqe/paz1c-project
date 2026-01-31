package org.openjfx.hellofx.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.openjfx.hellofx.App;
import org.openjfx.hellofx.utils.AuthService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> languageCombo;

    @FXML
    private ToggleButton themeToggle;

    @FXML
    private Label statusLabel;

    private final AuthService authService = new AuthService();

    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        if (statusLabel != null) {
            statusLabel.setText("");
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
        }
        setupLanguageSelector();
        setupThemeSelector();
    }

    @FXML
    void onLogin(ActionEvent event) {
        clearStatus();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            setStatus(get("login.error.fill"), false);
            return;
        }

        try {
            boolean ok = authService.login(username, password);
            if (ok) {
                App.setRoot("membership_view");
            } else {
                setStatus(get("login.error.invalid"), false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setStatus(get("login.error.failed") + ": " + e.getMessage(), false);
        } catch (IOException e) {
            e.printStackTrace();
            setStatus(get("login.error.load"), false);
        }
    }

    private void clearStatus() {
        if (statusLabel == null) return;
        statusLabel.setText("");
        statusLabel.getStyleClass().removeAll("status-error", "status-success");
        statusLabel.setStyle("");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    private void setStatus(String text, boolean success) {
        if (statusLabel == null) return;
        statusLabel.getStyleClass().removeAll("status-error", "status-success");
        statusLabel.getStyleClass().add(success ? "status-success" : "status-error");
        statusLabel.setStyle(success
            ? "-fx-text-fill: #22c55e;"
            : "-fx-text-fill: #ef4444;");
        statusLabel.setText(text);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }


    private String get(String key) {
        return resources != null && resources.containsKey(key) ? resources.getString(key) : key;
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
                statusLabel.setText(get("login.error.load"));
            }
        });
    }

    private void setupThemeSelector() {
        if (themeToggle == null) return;
        boolean isLight = App.getCurrentTheme().contains("light");
        themeToggle.setSelected(isLight);
        themeToggle.setText(isLight ? "Light" : "Dark");
        themeToggle.setOnAction(e -> {
            boolean nowLight = themeToggle.isSelected();
            themeToggle.setText(nowLight ? "Light" : "Dark");
            String themePath = nowLight
                    ? "/org/openjfx/hellofx/styles-light.css"
                    : "/org/openjfx/hellofx/styles.css";
            App.switchTheme(themePath);
        });
    }

}
