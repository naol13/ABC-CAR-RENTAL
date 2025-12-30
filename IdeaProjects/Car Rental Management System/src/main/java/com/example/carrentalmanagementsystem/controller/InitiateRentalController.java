package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CarDAO;
import com.example.carrentalmanagementsystem.dao.CustomerDAO;
import com.example.carrentalmanagementsystem.dao.RentalDAO;
import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.model.Customer;
import com.example.carrentalmanagementsystem.model.Rental;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class InitiateRentalController {

    // DAOs
    private final CarDAO carDAO = new CarDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final RentalDAO rentalDAO = new RentalDAO();

    // Customer Details
    @FXML private TextField customerSearchField;
    @FXML private ListView<Customer> customerListView;
    @FXML private Label selectedCustomerLabel;

    // Car Selection
    @FXML private TextField carSearchField;
    @FXML private ListView<Car> carListView;
    @FXML private Label selectedCarLabel;

    // Rental Period
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label durationLabel;

    // Extra Services & Discounts
    @FXML private CheckBox gpsCheckBox;
    @FXML private CheckBox childSeatCheckBox;
    @FXML private CheckBox insuranceCheckBox;
    @FXML private TextField discountField;

    // Rental Summary & Payment
    @FXML private VBox rentalSummaryBox;
    @FXML private Label totalAmountLabel;
    @FXML private TextField amountPaidField;
    @FXML private Button processPaymentButton;

    private ObservableList<Customer> customerList;
    private ObservableList<Car> carList;
    private Customer selectedCustomer;
    private Car selectedCar;

    @FXML
    public void initialize() {
        // Load initial data
        customerList = FXCollections.observableArrayList(customerDAO.getAllCustomers());
        carList = FXCollections.observableArrayList(carDAO.getAllCars().stream().filter(Car::isAvailable).toList());
        
        customerListView.setItems(customerList);
        carListView.setItems(carList);

        // Setup listeners
        setupSearchListeners();
        setupSelectionListeners();
        setupDateListeners();
        setupCostCalculationListeners();
    }

    private void setupSearchListeners() {
        customerSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            customerListView.setItems(customerList.filtered(c -> c.getName().toLowerCase().contains(newVal.toLowerCase()) || c.getContact().toLowerCase().contains(newVal.toLowerCase())));
        });

        carSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            carListView.setItems(carList.filtered(c -> c.getMake().toLowerCase().contains(newVal.toLowerCase()) || c.getModel().toLowerCase().contains(newVal.toLowerCase())));
        });
    }

    private void setupSelectionListeners() {
        customerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedCustomer = newVal;
            if (selectedCustomer != null) {
                selectedCustomerLabel.setText("Selected: " + selectedCustomer.getName());
                updateSummary();
            }
        });

        carListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedCar = newVal;
            if (selectedCar != null) {
                selectedCarLabel.setText("Selected: " + selectedCar.getMake() + " " + selectedCar.getModel());
                updateSummary();
            }
        });
    }

    private void setupDateListeners() {
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateDurationAndSummary());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateDurationAndSummary());
    }

    private void setupCostCalculationListeners() {
        gpsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateSummary());
        childSeatCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateSummary());
        insuranceCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateSummary());
        discountField.textProperty().addListener((obs, oldVal, newVal) -> updateSummary());
    }

    private void updateDurationAndSummary() {
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            long days = ChronoUnit.DAYS.between(startDatePicker.getValue(), endDatePicker.getValue());
            durationLabel.setText("Duration: " + days + " days");
        }
        updateSummary();
    }

    private void updateSummary() {
        rentalSummaryBox.getChildren().clear();
        double total = 0;

        if (selectedCar != null && startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            long days = ChronoUnit.DAYS.between(startDatePicker.getValue(), endDatePicker.getValue());
            if (days > 0) {
                double carCost = selectedCar.getDailyRate() * days; // Changed to getDailyRate()
                rentalSummaryBox.getChildren().add(new Label("Car Rental (" + days + " days): $" + carCost));
                total += carCost;
            }
        }

        if (gpsCheckBox.isSelected()) {
            rentalSummaryBox.getChildren().add(new Label("GPS Navigation: $10.00"));
            total += 10;
        }
        if (childSeatCheckBox.isSelected()) {
            rentalSummaryBox.getChildren().add(new Label("Child Seat: $15.00"));
            total += 15;
        }
        if (insuranceCheckBox.isSelected()) {
            rentalSummaryBox.getChildren().add(new Label("Premium Insurance: $50.00"));
            total += 50;
        }

        // Simple discount logic
        if (discountField.getText().equalsIgnoreCase("SAVE10")) {
            double discount = total * 0.1;
            rentalSummaryBox.getChildren().add(new Label("Discount (10%): -$" + String.format("%.2f", discount)));
            total -= discount;
        }

        totalAmountLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    private void handleProcessPayment() {
        if (selectedCustomer == null || selectedCar == null || startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Information", "Please select a customer, car, and rental period.");
            return;
        }

        double totalAmount = Double.parseDouble(totalAmountLabel.getText().replace("$", ""));
        double amountPaid = Double.parseDouble(amountPaidField.getText());

        if (amountPaid < totalAmount) {
            showAlert(Alert.AlertType.WARNING, "Payment Incomplete", "The amount paid is less than the total amount due.");
            return;
        }

        // Create and save the rental
        Rental newRental = new Rental(0, selectedCar.getId(), selectedCustomer.getId(), startDatePicker.getValue(), endDatePicker.getValue(), totalAmount, "Pending");
        rentalDAO.addRental(newRental);

        // Update car status
        selectedCar.setStatus("Rented");
        carDAO.updateCar(selectedCar);

        showAlert(Alert.AlertType.INFORMATION, "Payment Successful", "Rental agreement created successfully.");
        
        // Optionally, close the window or navigate away
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
