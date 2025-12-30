package com.example.carrentalmanagementsystem;

import com.example.carrentalmanagementsystem.util.CarDataSeeder;
import com.example.carrentalmanagementsystem.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class HelloApplication extends Application {

    @Override
    public void init() throws Exception {
        System.out.println("DIAGNOSTIC (HelloApplication.init): Calling DatabaseConnection.initializeDatabase().");
        DatabaseConnection.initializeDatabase();
        System.out.println("DIAGNOSTIC (HelloApplication.init): DatabaseConnection.initializeDatabase() completed.");

        CarDataSeeder.seedData();

        super.init();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Diagnostic: Check for background image
        URL bgUrl = getClass().getResource("/pic/background.jpg");
        if (bgUrl != null) {
            System.out.println("DIAGNOSTIC: Background image found at: " + bgUrl.toExternalForm());
        } else {
            System.err.println("DIAGNOSTIC: Background image NOT found at /pic/background.jpg");
        }

        // Set the application icon
        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Error loading application icon: " + e.getMessage());
        }

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("role-selection-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("ABC Car Rental - Select Role");
        stage.setScene(scene);
        stage.show();
    }
}
