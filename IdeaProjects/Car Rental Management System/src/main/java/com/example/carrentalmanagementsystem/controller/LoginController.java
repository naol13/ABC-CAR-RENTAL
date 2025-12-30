package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CustomerDAO;
import com.example.carrentalmanagementsystem.dao.UserDAO;
import com.example.carrentalmanagementsystem.dao.UserDAOImpl;
import com.example.carrentalmanagementsystem.model.Customer;
import com.example.carrentalmanagementsystem.model.User;
import com.example.carrentalmanagementsystem.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorMessageLabel;

    private UserDAO userDAO;
    private CustomerDAO customerDAO;

    public LoginController() {
        this.userDAO = new UserDAOImpl();
        this.customerDAO = new CustomerDAO();
    }

    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Basic client-side validation for empty fields
        if (username.isEmpty() || password.isEmpty()) {
            errorMessageLabel.setText("Username and password cannot be empty.");
            return;
        }

        // 1. Try to log in as an admin/staff
        User user = userDAO.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("Login successful for admin/staff: " + username);
            SessionManager.setCurrentUser(user);
            navigateToAdminDashboard(event);
            return;
        }

        // 2. If not an admin, try to log in as a customer
        Customer customer = customerDAO.getCustomerByUsername(username);
        if (customer != null && customer.getPassword() != null && customer.getPassword().equals(password)) {
            System.out.println("Login successful for customer: " + username);
            // We can reuse the SessionManager by creating a "User" object on the fly for the customer
            SessionManager.setCurrentUser(new User(customer.getId(), customer.getUsername(), null, "customer"));
            navigateToCustomerDashboard(event);
            return;
        }

        // 3. If both fail
        errorMessageLabel.setText("Invalid username or password.");
    }

    private void navigateToAdminDashboard(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/main-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/carrentalmanagementsystem/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Car Rental Management System");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorMessageLabel.setText("Error loading admin dashboard.");
        }
    }

    private void navigateToCustomerDashboard(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/customer-dashboard-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            // Optionally add a separate stylesheet for the customer view
            // scene.getStylesheets().add(getClass().getResource("/com/example/carrentalmanagementsystem/customer-style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Welcome, " + SessionManager.getCurrentUser().getUsername());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorMessageLabel.setText("Error loading customer dashboard.");
        }
    }
}
