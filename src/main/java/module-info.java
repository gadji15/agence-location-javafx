module agencelocation.agencelocationjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires jbcrypt;
    requires openhtmltopdf.pdfbox;

    opens agencelocation.agencelocationjavafx to javafx.fxml;
    exports agencelocation.agencelocationjavafx;
}