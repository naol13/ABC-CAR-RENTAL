package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CarDAO;
import com.example.carrentalmanagementsystem.dao.CustomerDAO;
import com.example.carrentalmanagementsystem.dao.RentalDAO;
import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.model.Customer;
import com.example.carrentalmanagementsystem.model.Rental;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;

import java.time.LocalDate;

public class RentalFormController {

    @FXML
    private ComboBox<Car> carComboBox;
    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private DatePicker rentalDatePicker;

    private RentalDAO rentalDAO;
    private CarDAO carDAO;
    private CustomerDAO customerDAO;
    private MainController mainController;
    private Stage stage;
    private Rental rental;

    @FXML
    public void initialize() {
        rentalDAO = new RentalDAO();
        carDAO = new CarDAO();
        customerDAO = new CustomerDAO();

        // Populate ComboBoxes with available cars and all customers
        carComboBox.setItems(FXCollections.observableArrayList(carDAO.getAllCars().stream().filter(Car::isAvailable).toList()));
        customerComboBox.setItems(FXCollections.observableArrayList(customerDAO.getAllCustomers()));

        // Customize Car ComboBox display
        carComboBox.setCellFactory(lv -> new ListCell<Car>() {
            @Override
            protected void updateItem(Car car, boolean empty) {
                super.updateItem(car, empty);
                setText(empty ? "" : car.getMake() + " " + car.getModel() + " (" + car.getYear() + ")");
            }
        });
        carComboBox.setButtonCell(new ListCell<Car>() {
            @Override
            protected void updateItem(Car car, boolean empty) {
                super.updateItem(car, empty);
                setText(empty ? "" : car.getMake() + " " + car.getModel() + " (" + car.getYear() + ")");
            }
        });

        // Customize Customer ComboBox display
        customerComboBox.setCellFactory(lv -> new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                setText(empty ? "" : customer.getName() + " (License: " + customer.getDriversLicense() + ")");
            }
        });
        customerComboBox.setButtonCell(new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                setText(empty ? "" : customer.getName() + " (License: " + customer.getDriversLicense() + ")");
            }
        });

        // Set default rental date to today
        rentalDatePicker.setValue(LocalDate.now());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setRental(Rental rental) {
        this.rental = rental;
        if (rental != null) {
            // Pre-select the car, customer, and date for editing
            carComboBox.getSelectionModel().select(carDAO.getAllCars().stream().filter(c -> c.getId() == rental.getCarId()).findFirst().orElse(null));
            customerComboBox.getSelectionModel().select(customerDAO.getAllCustomers().stream().filter(c -> c.getId() == rental.getCustomerId()).findFirst().orElse(null));
            rentalDatePicker.setValue(rental.getRentalDate());
        }
    }

    @FXML
    private void handleSave() {
        Car selectedCar = carComboBox.getSelectionModel().getSelectedItem();
        Customer selectedCustomer = customerComboBox.getSelectionModel().getSelectedItem();
        LocalDate rentalDate = rentalDatePicker.getValue();

        if (selectedCar == null || selectedCustomer == null || rentalDate == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please select a car, a customer, and a rental date.");
            return;
        }

        // Validate that the rental date is not in the past
        if (rentalDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date", "Rental date cannot be in the past.");
            return;
        }

        if (rental == null) {
            // Add new rental
            rental = new Rental(0, selectedCar.getId(), selectedCustomer.getId(), rentalDate, null, selectedCar.getDailyRate(), "Pending");
            rentalDAO.addRental(rental);

            // Mark the car as unavailable
            selectedCar.setStatus("Rented");
            carDAO.updateCar(selectedCar);
        } else {
            // Update existing rental
            rental.setCarId(selectedCar.getId());
            rental.setCustomerId(selectedCustomer.getId());
            rental.setRentalDate(rentalDate);
            rental.setFinalPrice(selectedCar.getDailyRate());
            rentalDAO.updateRental(rental);
        }

        if (mainController != null) {
            mainController.refreshAllViews();
        }
        stage.close();
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
