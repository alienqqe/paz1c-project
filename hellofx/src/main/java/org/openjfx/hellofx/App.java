package org.openjfx.hellofx;

import java.io.IOException;
import java.sql.SQLException;

import org.openjfx.hellofx.utils.AuthService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            new AuthService().ensureDefaultAdmin();
        } catch (SQLException e) {
            showInitError(e);
            return;
        }

        scene = new Scene(loadFXML("login_view"), 640, 480);
        stage.setScene(scene);
        stage.setTitle("Gym Login");
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    private void showInitError(Exception e) {
        e.printStackTrace();
    }

    public static void main(String[] args) {
        launch();
    }

}
