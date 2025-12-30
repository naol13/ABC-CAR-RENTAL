package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.model.Car;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CustomerInitiateRentalController {

    @FXML
    private Label carDetailsLabel;
    @FXML
    private Label dailyRateLabel;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label durationLabel;
    @FXML
    private Label totalCostLabel;

    private Car selectedCar;
    private Stage dialogStage; // This is the stage for the current dialog (customer-initiate-rental-view)

    public void setCar(Car car) {
        this.selectedCar = car;
        carDetailsLabel.setText(car.getMake() + " " + car.getModel() + " (" + car.getYear() + ")");
        dailyRateLabel.setText(String.format("Daily Rate: $%.2f", car.getDailyRate()));
        calculateCost(); // Recalculate if car is set after dates
    }

    public void setDates(LocalDate start, LocalDate end) {
        this.startDatePicker.setValue(start);
        this.endDatePicker.setValue(end);
        calculateCost();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    public void initialize() {
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateCost());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateCost());
    }

    private void calculateCost() {
        if (selectedCar == null || startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            durationLabel.setText("Duration: 0 days");
            totalCostLabel.setText("Total Cost: $0.00");
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Dates", "Start date cannot be after end date.");
            durationLabel.setText("Duration: 0 days");
            totalCostLabel.setText("Total Cost: $0.00");
            return;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1; // +1 to include the end day
        if (days <= 0) {
            days = 1; // Minimum 1 day rental
        }

        double totalCost = selectedCar.getDailyRate() * days;

        durationLabel.setText("Duration: " + days + " days");
        totalCostLabel.setText(String.format("Total Cost: $%.2f", totalCost));
    }

    @FXML
    private void handleNext(ActionEvent event) { // Renamed from handleConfirmRental
        System.out.println("DEBUG: handleNext (InitiateRental) started.");
        if (selectedCar == null || startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Information", "Please select rental dates.");
            System.out.println("DEBUG: Missing information for rental.");
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Dates", "Start date cannot be after end date.");
            System.out.println("DEBUG: Invalid dates selected.");
            return;
        }
        
        // Navigate to the next step (Personal Details)
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/customer-rental-details-view.fxml"));
            Parent parent = fxmlLoader.load();
            CustomerRentalDetailsController controller = fxmlLoader.getController();
            
            // Pass data to the next controller
            controller.setRentalData(selectedCar, startDate, endDate);
            
            // Use the same stage to replace the current scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.setTitle("Your Rental Details");
            // No need to showAndWait as we are replacing the scene, not opening a new dialog
            System.out.println("DEBUG: Navigated to customer-rental-details-view.fxml");

        } catch (IOException e) {
            System.err.println("ERROR: Exception during navigation to rental details: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not proceed to rental details.");
        }
    }

    @FXML
    private void handleCancel() {
        System.out.println("DEBUG: Rental initiation cancelled.");
        dialogStage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
