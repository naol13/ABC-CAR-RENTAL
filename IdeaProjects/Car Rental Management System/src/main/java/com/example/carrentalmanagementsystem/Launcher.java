package com.example.carrentalmanagementsystem;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // REMOVED: DatabaseConnection.initializeDatabase() call. Now handled in HelloApplication.init()
        Application.launch(HelloApplication.class, args);
    }
}
