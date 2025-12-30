package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CustomerDAO;
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
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CustomerLoginController {

    @FXML
    private TextField identifierField; // Changed from usernameField
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorMessageLabel;

    private CustomerDAO customerDAO;

    public CustomerLoginController() {
        this.customerDAO = new CustomerDAO();
    }

    @FXML
    private void handleLoginButton(ActionEvent event) {
        String identifier = identifierField.getText();
        String password = passwordField.getText();

        Customer customer = customerDAO.getCustomerByIdentifier(identifier);
        if (customer != null && customer.getPassword() != null && customer.getPassword().equals(password)) {
            loginAndNavigate(customer, event);
        } else {
            errorMessageLabel.setText("Invalid credentials.");
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/customer-form.fxml"));
            Parent parent = fxmlLoader.load();
            
            CustomerFormController customerFormController = fxmlLoader.getController();
            customerFormController.setLoginController(this);

            Stage stage = new Stage();
            stage.setTitle("Customer Sign Up");
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Set the icon for the sign-up stage
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Error loading application icon for sign-up: " + e.getMessage());
            }

            Scene scene = new Scene(parent);
            stage.setScene(scene);
            
            customerFormController.setStage(stage);

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loginAndNavigate(Customer customer, ActionEvent event) {
        System.out.println("Login successful for customer: " + customer.getUsername());
        SessionManager.setCurrentUser(new User(customer.getId(), customer.getUsername(), null, "customer"));
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        navigateToCustomerDashboard(stage);
    }
    
    public void loginAndNavigate(Customer customer, Stage stage) {
        System.out.println("Login successful for customer: " + customer.getUsername());
        SessionManager.setCurrentUser(new User(customer.getId(), customer.getUsername(), null, "customer"));
        navigateToCustomerDashboard(stage);
    }

    private void navigateToCustomerDashboard(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/customer-dashboard-view.fxml"));
            Parent root = fxmlLoader.load();

            // Set the icon for the new stage
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Error loading application icon for customer dashboard: " + e.getMessage());
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Welcome, " + SessionManager.getCurrentUser().getUsername());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorMessageLabel.setText("Error loading customer dashboard.");
        }
    }
}
