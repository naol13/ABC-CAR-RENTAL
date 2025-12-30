package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.UserDAO;
import com.example.carrentalmanagementsystem.dao.UserDAOImpl;
import com.example.carrentalmanagementsystem.model.User;
import com.example.carrentalmanagementsystem.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class AdminLoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private Button loginButton;

    private UserDAO userDAO;

    public AdminLoginController() {
        System.out.println("DIAGNOSTIC: AdminLoginController constructor called. Instantiating UserDAOImpl.");
        this.userDAO = new UserDAOImpl();
    }

    @FXML
    public void initialize() {
        // Handle Enter key on username field to move to password field
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });

        // Handle Enter key on password field to trigger login
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire();
            }
        });
    }

    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        System.out.println("DIAGNOSTIC: Admin login attempt for username: " + username);

        User user = userDAO.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("Login successful for admin/staff: " + username);
            SessionManager.setCurrentUser(user);
            navigateToAdminDashboard(event);
        } else {
            System.out.println("DIAGNOSTIC: Admin login failed. Invalid credentials.");
            errorMessageLabel.setText("Invalid username or password.");
        }
    }

    private void navigateToAdminDashboard(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/main-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Set the icon for the new stage
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Error loading application icon for admin dashboard: " + e.getMessage());
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/carrentalmanagementsystem/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("ABC Car Rental");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorMessageLabel.setText("Error loading admin dashboard.");
        }
    }
}
