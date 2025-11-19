module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires transitive javafx.graphics;
    requires io.github.cdimascio.dotenv.java;
    
     opens org.openjfx.hellofx.controllers to javafx.fxml;
    exports org.openjfx.hellofx;
}