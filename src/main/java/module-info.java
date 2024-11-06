module com.teamobi.map {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    exports com.teamobi.map;
    exports com.teamobi.map.model;
    exports com.teamobi.map.controller;
    opens com.teamobi.map.controller to javafx.fxml;
}
