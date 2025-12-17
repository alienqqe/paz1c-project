module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires transitive javafx.graphics;
    requires io.github.cdimascio.dotenv.java;
    requires jbcrypt;
    requires spring.jdbc;
    requires spring.core;
    requires spring.beans;
    requires spring.tx;
    requires com.zaxxer.hikari;
    opens org.openjfx.hellofx to javafx.fxml;
    opens org.openjfx.hellofx.entities to javafx.base;
    opens org.openjfx.hellofx.dao to spring.core, spring.beans;

    opens org.openjfx.hellofx.controllers to javafx.fxml, javafx.base;
    opens org.openjfx.hellofx.utils to javafx.fxml;
    exports org.openjfx.hellofx;
}
