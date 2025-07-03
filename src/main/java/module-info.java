module com.doebi.tools.loratagmate {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires javafx.swing;

    opens com.doebi.tools.loratagmate to javafx.fxml;
    exports com.doebi.tools.loratagmate;
}