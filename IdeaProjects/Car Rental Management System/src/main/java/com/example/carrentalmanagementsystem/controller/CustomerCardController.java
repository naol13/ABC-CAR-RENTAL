package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.model.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CustomerCardController {

    @FXML
    private Label nameLabel;
    @FXML
    private Label driversLicenseLabel;
    @FXML
    private Label contactLabel;
    @FXML
    private Label totalRentalsLabel;
    @FXML
    private Label statusLabel;

    private Customer customer;
    private CustomerManagementController customerManagementController;

    public void setData(Customer customer, CustomerManagementController customerManagementController) {
        this.customer = customer;
        this.customerManagementController = customerManagementController;

        nameLabel.setText(customer.getName());
        driversLicenseLabel.setText(customer.getDriversLicense());
        contactLabel.setText(customer.getContact());
        totalRentalsLabel.setText(String.valueOf(customer.getTotalRentals()));
        statusLabel.setText(customer.getStatus());

        // Set status style
        if ("Active".equalsIgnoreCase(customer.getStatus())) {
            statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;"); // Green
        } else {
            statusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // Red
        }
    }

    @FXML
    private void handleEdit() {
        customerManagementController.handleEditCustomer(customer);
    }

    @FXML
    private void handleDelete() {
        customerManagementController.handleDeleteCustomer(customer);
    }
}
