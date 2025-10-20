module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires transitive javafx.graphics;
    
     opens org.openjfx.hellofx.controllers to javafx.fxml;
    exports org.openjfx.hellofx;
}