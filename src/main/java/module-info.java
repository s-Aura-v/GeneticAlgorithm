module com.example.geneticalgorithmfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.desktop;
    requires jdk.xml.dom;

    opens com.example.geneticalgorithmfx to javafx.fxml;
    exports com.example.geneticalgorithmfx;
}