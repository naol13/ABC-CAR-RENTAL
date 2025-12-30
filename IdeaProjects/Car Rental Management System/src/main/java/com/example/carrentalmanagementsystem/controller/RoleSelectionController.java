package com.example.carrentalmanagementsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RoleSelectionController {

    @FXML
    private void handleAdminLogin(ActionEvent event) {
        navigateToLogin(event, "/com/example/carrentalmanagementsystem/admin-login-view.fxml", "Admin Login");
    }

    @FXML
    private void handleCustomerLogin(ActionEvent event) {
        navigateToLogin(event, "/com/example/carrentalmanagementsystem/customer-login-view.fxml", "Customer Login");
    }

    private void navigateToLogin(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/carrentalmanagementsystem/style.css").toExternalForm()); // Apply stylesheet
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error loading the view
        }
    }
}
