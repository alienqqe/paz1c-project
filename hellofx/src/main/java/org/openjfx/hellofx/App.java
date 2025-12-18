package org.openjfx.hellofx;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.openjfx.hellofx.utils.AuthService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene scene;
    private static Locale currentLocale = Locale.getDefault();
    private static String currentView = "login_view";
    private static String currentTheme = "/org/openjfx/hellofx/styles.css";

    @Override
    public void start(Stage stage) throws IOException {
        Locale.setDefault(currentLocale);
        try {
            new AuthService().ensureDefaultAdmin();
        } catch (SQLException e) {
            showInitError(e);
            return;
        }

        scene = new Scene(loadFXML("login_view"), 640, 480);
        applyTheme(scene);
        stage.setScene(scene);
        stage.setTitle(getBundle().getString("app.title"));
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        currentView = fxml;
        scene.setRoot(loadFXML(fxml));
        applyTheme(scene);
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"), getBundle());
        return fxmlLoader.load();
    }

    public static ResourceBundle getBundle() {
        ResourceBundle bundle = loadBundle(currentLocale);
        if (bundle == null && !Locale.ENGLISH.equals(currentLocale)) {
            bundle = loadBundle(Locale.ENGLISH);
        }
        return bundle == null
                ? ResourceBundle.getBundle("org.openjfx.hellofx.messages", Locale.ENGLISH)
                : bundle;
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static String getCurrentTheme() {
        return currentTheme;
    }

    public static void switchTheme(String themePath) {
        if (themePath == null) return;
        currentTheme = themePath;
        applyTheme(scene);
    }

    public static void applyTheme(Scene target) {
        if (target == null) return;
        String css = App.class.getResource(currentTheme).toExternalForm();
        target.getStylesheets().setAll(css);
        if (target.getRoot() != null) {
            target.getRoot().getStylesheets().setAll(css);
        }
    }

    public static void switchLocale(Locale locale) throws IOException {
        if (locale == null) return;
        // Clear cached bundles so the next load picks up the new locale.
        ResourceBundle.clearCache();
        currentLocale = locale;
        Locale.setDefault(locale);
        setRoot(currentView);
        if (scene != null && scene.getWindow() instanceof Stage stage) {
            var bundle = getBundle();
            if (bundle != null && bundle.containsKey("app.title")) {
                stage.setTitle(bundle.getString("app.title"));
            }
        }
    }

    // Manual UTF-8 bundle loader (avoids ResourceBundle.Control restriction in named modules)
    private static ResourceBundle loadBundle(Locale locale) {
        String base = "org/openjfx/hellofx/messages";
        String lang = locale.getLanguage();
        String resourceName = base + "_" + lang + ".properties";
        ResourceBundle bundle = readBundle(resourceName);
        if (bundle == null) {
            bundle = readBundle(base + ".properties");
        }
        return bundle;
    }

    private static ResourceBundle readBundle(String resourceName) {
        // Use App.class to locate resources so it works in modular runs.
        try (InputStream stream = App.class.getResourceAsStream("/" + resourceName)) {
            if (stream == null) return null;
            try (InputStreamReader reader = new InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)) {
                return new PropertyResourceBundle(reader);
            }
        } catch (IOException e) {
            return null;
        }
    }

    private void showInitError(Exception e) {
        e.printStackTrace();
    }

    public static void main(String[] args) {
        launch();
    }

}
