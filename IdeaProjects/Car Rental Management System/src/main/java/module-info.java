


module com.example.carrentalmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;

    opens com.example.carrentalmanagementsystem to javafx.fxml;
    opens com.example.carrentalmanagementsystem.controller to javafx.fxml, javafx.base; // Allow javafx.base to access the controller package
    opens com.example.carrentalmanagementsystem.model to javafx.base;
    exports com.example.carrentalmanagementsystem;
}
