package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CarDAO;
import com.example.carrentalmanagementsystem.dao.RentalDAO;
import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.model.Rental;
import com.example.carrentalmanagementsystem.util.CarDataService;
import com.example.carrentalmanagementsystem.util.NotificationService;
import com.example.carrentalmanagementsystem.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CustomerRentalConfirmationController {

    @FXML
    private Label carSummaryLabel;
    @FXML
    private Label dailyRateSummaryLabel;
    @FXML
    private Label datesSummaryLabel;
    @FXML
    private Label durationSummaryLabel;
    @FXML
    private Label totalCostSummaryLabel;
    @FXML
    private Label addressSummaryLabel;

    private Car selectedCar;
    private LocalDate startDate;
    private LocalDate endDate;
    private String idImagePath;
    private String street;
    private String city;
    private String state;

    private CarDAO carDAO = new CarDAO();
    private RentalDAO rentalDAO = new RentalDAO();

    public void setRentalData(Car car, LocalDate start, LocalDate end, String idImgPath, String str, String cty, String st) {
        this.selectedCar = car;
        this.startDate = start;
        this.endDate = end;
        this.idImagePath = idImgPath;
        this.street = str;
        this.city = cty;
        this.state = st;

        updateSummaryLabels();
    }

    private void updateSummaryLabels() {
        if (selectedCar != null) {
            carSummaryLabel.setText(String.format("Car: %s %s (%d)", selectedCar.getMake(), selectedCar.getModel(), selectedCar.getYear()));
            dailyRateSummaryLabel.setText(String.format("Daily Rate: $%.2f", selectedCar.getDailyRate()));
        }
        if (startDate != null && endDate != null) {
            datesSummaryLabel.setText(String.format("From: %s To: %s", startDate.toString(), endDate.toString()));
            long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            durationSummaryLabel.setText("Duration: " + days + " days");
            double totalCost = selectedCar.getDailyRate() * days;
            totalCostSummaryLabel.setText(String.format("Total Cost: $%.2f", totalCost));
        }
        addressSummaryLabel.setText(String.format("Address: %s, %s, %s", street, city, state));
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/customer-rental-details-view.fxml"));
            Parent root = fxmlLoader.load();
            CustomerRentalDetailsController controller = fxmlLoader.getController();
            controller.setRentalData(selectedCar, startDate, endDate);
            controller.prefillDetails(idImagePath, street, city, state);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Your Rental Details");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not go back to previous step.");
        }
    }

    @FXML
    private void handleConfirmAndPay(ActionEvent event) {
        if (selectedCar == null || startDate == null || endDate == null || idImagePath == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Information", "Some rental details are missing. Please go back and fill them.");
            return;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days <= 0) {
            days = 1;
        }
        double totalCost = selectedCar.getDailyRate() * days;

        int customerId = -1;
        if (SessionManager.getCurrentUser() != null) {
            customerId = SessionManager.getCurrentUser().getId();
        } else {
            showAlert(Alert.AlertType.ERROR, "Session Error", "Customer session not found. Please log in again.");
            return;
        }

        // Create the rental record with "Pending" status
        Rental newRental = new Rental(
                0, // ID will be auto-generated
                selectedCar.getId(),
                customerId,
                startDate,
                endDate,
                totalCost,
                "Pending"
        );

        try {
            rentalDAO.addRental(newRental);
            
            // Update car status to "Pending" immediately
            selectedCar.setStatus("Pending");
            carDAO.updateCar(selectedCar);

            NotificationService.getInstance().addRentalRequest(newRental);
            
            // Refresh available cars list for the customer
            CarDataService.getInstance().refreshAvailableCars();

            showAlert(Alert.AlertType.INFORMATION, "Rental Request Submitted", "Your rental is processed successfully! Your car will arrive in 30 minutes after admin approval.");
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            System.err.println("ERROR: Exception during rental confirmation: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Rental Error", "An error occurred while submitting your rental request. Please try again.");
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
