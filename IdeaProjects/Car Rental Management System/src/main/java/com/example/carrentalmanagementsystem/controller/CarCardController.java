package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.model.Car;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.MalformedURLException;

public class CarCardController {

    @FXML
    private VBox carCard;
    @FXML
    private ImageView carImageView;
    @FXML
    private Label modelLabel;
    @FXML
    private Label yearLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private HBox actionButtonsHBox; // Injected HBox for action buttons
    @FXML
    private Button editButton; // Injected Edit button
    @FXML
    private Button deleteButton; // Injected Delete button
    @FXML
    private Button rentButton; // Injected Rent button

    private Car car;
    private CarManagementController carManagementController; // For admin context
    private CustomerDashboardController customerDashboardController; // For customer context

    // Method for Admin context
    public void setData(Car car, CarManagementController carManagementController) {
        this.car = car;
        this.carManagementController = carManagementController;
        updateUI();
        // Show admin buttons, hide rent button
        editButton.setVisible(true);
        editButton.setManaged(true);
        deleteButton.setVisible(true);
        deleteButton.setManaged(true);
        rentButton.setVisible(false);
        rentButton.setManaged(false);
    }

    // Method for Customer context
    public void setData(Car car, CustomerDashboardController customerDashboardController) {
        this.car = car;
        this.customerDashboardController = customerDashboardController;
        updateUI();
        // Hide admin buttons, show rent button
        editButton.setVisible(false);
        editButton.setManaged(false);
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);
        rentButton.setVisible(true);
        rentButton.setManaged(true);
    }

    private void updateUI() {
        modelLabel.setText(car.getMake() + " " + car.getModel());
        yearLabel.setText(String.valueOf(car.getYear()));
        priceLabel.setText(String.format("$%.2f/day", car.getDailyRate()));
        statusLabel.setText(car.getStatus());

        String status = car.getStatus();
        boolean isAvailable = "Available".equalsIgnoreCase(status);
        boolean isRented = "Rented".equalsIgnoreCase(status);
        boolean isMaintenance = "Under Maintenance".equalsIgnoreCase(status) || "Maintenance".equalsIgnoreCase(status);
        boolean isPending = "Pending".equalsIgnoreCase(status);

        // Set status style
        if (isAvailable) {
            statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;"); // Green
        } else if (isPending) {
            statusLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;"); // Orange/Yellow
        } else { // Rented, Under Maintenance, etc.
            statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // Red
        }

        // Admin Buttons Logic: Disable if Rented or Under Maintenance
        if (editButton != null && deleteButton != null) {
            if (isRented || isMaintenance) {
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            } else {
                editButton.setDisable(false);
                deleteButton.setDisable(false);
            }
        }

        // Customer Rent Button Logic
        if (rentButton != null) {
            if (isAvailable) {
                rentButton.setDisable(false);
            } else {
                rentButton.setDisable(true);
            }
        }

        // Load image
        if (car.getImagePath() != null && !car.getImagePath().isEmpty()) {
            try {
                // Check if it's a URL or a local file path
                if (car.getImagePath().startsWith("http")) {
                    carImageView.setImage(new Image(car.getImagePath()));
                } else {
                    File file = new File(car.getImagePath());
                    if (file.exists()) {
                        carImageView.setImage(new Image(file.toURI().toURL().toString()));
                    } else {
                        setDefaultImage();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                setDefaultImage();
            }
        } else {
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        carImageView.setImage(null); // Clear any previous image
    }

    @FXML
    private void handleEdit() {
        if (carManagementController != null) {
            carManagementController.handleEditCar(car);
        }
    }

    @FXML
    private void handleDelete() {
        if (carManagementController != null) {
            carManagementController.handleDeleteCar(car);
        }
    }

    @FXML
    private void handleRent(ActionEvent event) {
        if (customerDashboardController != null) {
            customerDashboardController.openCustomerRentalForm(car, event);
        }
    }
}
