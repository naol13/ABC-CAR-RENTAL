package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CarDAO;
import com.example.carrentalmanagementsystem.dao.MaintenanceDAO;
import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.model.Maintenance;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class MaintenanceFormController {

    @FXML
    private ComboBox<Car> carComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField costField;

    private MaintenanceDAO maintenanceDAO;
    private CarDAO carDAO;
    private MainController mainController;
    private Stage stage;
    private Maintenance maintenance;

    @FXML
    public void initialize() {
        maintenanceDAO = new MaintenanceDAO();
        carDAO = new CarDAO();
        carComboBox.setItems(FXCollections.observableArrayList(carDAO.getAllCars()));
        startDatePicker.setValue(LocalDate.now());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setMaintenance(Maintenance maintenance) {
        this.maintenance = maintenance;
        if (maintenance != null) {
            carComboBox.getSelectionModel().select(carDAO.getAllCars().stream().filter(c -> c.getId() == maintenance.getCarId()).findFirst().orElse(null));
            startDatePicker.setValue(maintenance.getStartDate());
            endDatePicker.setValue(maintenance.getEndDate());
            descriptionArea.setText(maintenance.getDescription());
            costField.setText(String.valueOf(maintenance.getCost()));
        }
    }

    @FXML
    private void handleSave() {
        if (!isInputValid()) {
            return;
        }

        Car selectedCar = carComboBox.getSelectionModel().getSelectedItem();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String description = descriptionArea.getText().trim();
        double cost = Double.parseDouble(costField.getText().trim());

        if (maintenance == null) {
            // Add new maintenance record
            maintenance = new Maintenance(0, selectedCar.getId(), startDate, endDate, description, cost);
            maintenanceDAO.addMaintenance(maintenance);
            selectedCar.setStatus("Under Maintenance");
            carDAO.updateCar(selectedCar);
        } else {
            // Update existing maintenance record
            maintenance.setCarId(selectedCar.getId());
            maintenance.setStartDate(startDate);
            maintenance.setEndDate(endDate);
            maintenance.setDescription(description);
            maintenance.setCost(cost);
            maintenanceDAO.updateMaintenance(maintenance);

            if (maintenance.getEndDate() != null && !maintenance.getEndDate().isAfter(LocalDate.now())) { // If maintenance is finished
                selectedCar.setStatus("Available");
                carDAO.updateCar(selectedCar);
            } else if (maintenance.getEndDate() == null || maintenance.getEndDate().isAfter(LocalDate.now())) { // If maintenance is ongoing or future
                selectedCar.setStatus("Under Maintenance");
                carDAO.updateCar(selectedCar);
            }
        }

        if (mainController != null) {
            mainController.refreshAllViews();
        }
        stage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (carComboBox.getSelectionModel().getSelectedItem() == null) {
            errorMessage += "Please select a car!\n";
        }

        LocalDate startDate = startDatePicker.getValue();
        if (startDate == null) {
            errorMessage += "Start Date cannot be empty!\n";
        } else if (startDate.isBefore(LocalDate.now()) && maintenance == null) { // For new records, start date cannot be in the past
            errorMessage += "Start Date cannot be in the past for a new maintenance record!\n";
        }

        LocalDate endDate = endDatePicker.getValue();
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            errorMessage += "End Date cannot be before Start Date!\n";
        }

        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            errorMessage += "Description cannot be empty!\n";
        }

        double cost = 0.0;
        try {
            cost = Double.parseDouble(costField.getText().trim());
            if (cost < 0) {
                errorMessage += "Cost cannot be negative!\n";
            }
        } catch (NumberFormatException e) {
            errorMessage += "Cost must be a valid number!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Invalid Fields", errorMessage);
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
