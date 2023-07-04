module classes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;


    opens classes to javafx.fxml;
    exports classes;
}
