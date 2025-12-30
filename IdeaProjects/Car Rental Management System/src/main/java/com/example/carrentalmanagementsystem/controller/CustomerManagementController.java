package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CustomerDAO;
import com.example.carrentalmanagementsystem.model.Customer;
import com.example.carrentalmanagementsystem.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class CustomerManagementController {

    @FXML
    private TilePane customerTilePane;
    @FXML
    private TextField searchField;

    private CustomerDAO customerDAO;
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredData;
    private MainController mainController;

    @FXML
    public void initialize() {
        customerDAO = new CustomerDAO();
        filteredData = new FilteredList<>(customerList, p -> true);
        setupSearch();
        refreshTable();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        Predicate<Customer> searchPredicate = customer -> {
            if (searchText.isEmpty()) {
                return true;
            }
            return customer.getName().toLowerCase().contains(searchText) ||
                   customer.getContact().toLowerCase().contains(searchText) ||
                   customer.getEmail().toLowerCase().contains(searchText); // Added email to search
        };
        filteredData.setPredicate(searchPredicate);
        loadCustomerCards();
    }

    public void refreshTable() {
        System.out.println("DIAGNOSTIC (CustomerManagementController): refreshTable() called.");
        Task<List<Customer>> task = new Task<>() {
            @Override
            protected List<Customer> call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    System.out.println("DIAGNOSTIC (CustomerManagementController): CustomerDAO connection URL: " + conn.getMetaData().getURL());
                } catch (SQLException e) {
                    System.err.println("DIAGNOSTIC (CustomerManagementController): Error getting connection URL: " + e.getMessage());
                }
                List<Customer> fetchedCustomers = customerDAO.getAllCustomers();
                System.out.println("DIAGNOSTIC (CustomerManagementController): customerDAO.getAllCustomers() returned " + fetchedCustomers.size() + " customers.");
                return fetchedCustomers;
            }
        };

        task.setOnSucceeded(event -> {
            customerList.setAll(task.getValue());
            System.out.println("DIAGNOSTIC (CustomerManagementController): customerList updated. Now contains " + customerList.size() + " customers.");
            loadCustomerCards();
        });

        task.setOnFailed(event -> {
            System.err.println("DIAGNOSTIC (CustomerManagementController): Failed to load customer data task.");
            event.getSource().getException().printStackTrace();
            customerTilePane.getChildren().clear();
            customerTilePane.getChildren().add(new Label("Failed to load customer data."));
        });

        new Thread(task).start();
    }

    private void loadCustomerCards() {
        System.out.println("DIAGNOSTIC (CustomerManagementController): loadCustomerCards() called. Displaying " + filteredData.size() + " customers.");
        customerTilePane.getChildren().clear();
        for (Customer customer : filteredData) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/customer-card.fxml"));
                VBox card = loader.load();
                CustomerCardController controller = loader.getController();
                controller.setData(customer, this);
                customerTilePane.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAddNewCustomer() {
        if (mainController != null) {
            mainController.openCustomerForm(null);
        }
    }

    public void handleEditCustomer(Customer customer) {
        if (customer != null && mainController != null) {
            mainController.openCustomerForm(customer);
        }
    }

    public void handleDeleteCustomer(Customer customer) {
        if (customer != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Customer: " + customer.getName());
            alert.setContentText("Are you sure you want to delete this customer record?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                customerDAO.deleteCustomer(customer.getId()); // Calling the correct method
                if (mainController != null) {
                    mainController.refreshAllViews();
                } else {
                    refreshTable();
                }
                showAlert(Alert.AlertType.INFORMATION, "Deletion Successful", "Customer deleted successfully.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
