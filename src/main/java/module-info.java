module com.crud.ithreeamcrud {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires javafx.graphics;
    requires java.desktop;

    opens com.crud.ithreeamcrud to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.crud.ithreeamcrud;
}