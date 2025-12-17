package org.openjfx.hellofx;

import java.io.IOException;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        stage.setScene(scene);
        stage.setTitle(getBundle().getString("app.title"));
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        currentView = fxml;
        scene.setRoot(loadFXML(fxml));
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
        return bundle == null ? ResourceBundle.getBundle("org.openjfx.hellofx.messages") : bundle;
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static void switchLocale(Locale locale) throws IOException {
        if (locale == null) return;
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
        try (InputStream stream = App.class.getClassLoader().getResourceAsStream(resourceName)) {
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
