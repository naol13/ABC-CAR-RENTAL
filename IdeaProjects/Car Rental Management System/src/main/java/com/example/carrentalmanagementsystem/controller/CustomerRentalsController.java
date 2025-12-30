package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CarDAO;
import com.example.carrentalmanagementsystem.dao.CustomerDAO;
import com.example.carrentalmanagementsystem.dao.RentalDAO;
import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.model.Customer;
import com.example.carrentalmanagementsystem.model.Rental;
import com.example.carrentalmanagementsystem.model.RentalViewModel;
import com.example.carrentalmanagementsystem.util.CarDataService;
import com.example.carrentalmanagementsystem.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class CustomerRentalsController {

    @FXML
    private TableView<RentalViewModel> rentalsTable;
    @FXML
    private TableColumn<RentalViewModel, String> carMakeColumn;
    @FXML
    private TableColumn<RentalViewModel, String> carModelColumn;
    @FXML
    private TableColumn<RentalViewModel, LocalDate> rentalDateColumn;
    @FXML
    private TableColumn<RentalViewModel, Double> dailyRateColumn;
    @FXML
    private TableColumn<RentalViewModel, String> statusColumn;
    @FXML
    private Button returnButton;

    private RentalDAO rentalDAO = new RentalDAO();
    private CarDAO carDAO = new CarDAO();
    private CustomerDAO customerDAO = new CustomerDAO();

    @FXML
    public void initialize() {
        carMakeColumn.setCellValueFactory(new PropertyValueFactory<>("carMake"));
        carModelColumn.setCellValueFactory(new PropertyValueFactory<>("carModel"));
        rentalDateColumn.setCellValueFactory(new PropertyValueFactory<>("rentalDate"));
        dailyRateColumn.setCellValueFactory(new PropertyValueFactory<>("dailyRate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        rentalsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String status = newSelection.getStatus();
                System.out.println("DIAGNOSTIC: Selected rental status: '" + status + "'");
                
                if ("Active".equals(status)) {
                    returnButton.setDisable(false);
                    returnButton.setText("Return Selected Car");
                    System.out.println("DIAGNOSTIC: Button enabled (Return).");
                } else if ("Pending".equals(status)) {
                    returnButton.setDisable(false);
                    returnButton.setText("Cancel Request");
                    System.out.println("DIAGNOSTIC: Button enabled (Cancel).");
                } else {
                    returnButton.setDisable(true);
                    returnButton.setText("Return Selected Car");
                    System.out.println("DIAGNOSTIC: Button disabled (Status not Active/Pending).");
                }
            } else {
                returnButton.setDisable(true);
                returnButton.setText("Return Selected Car");
                System.out.println("DIAGNOSTIC: Button disabled (No selection).");
            }
        });

        loadRentals();
    }

    private void loadRentals() {
        Customer currentCustomer = customerDAO.getCustomerByUsername(SessionManager.getCurrentUser().getUsername());
        if (currentCustomer == null) {
            System.err.println("Could not load rentals: Customer not found.");
            return;
        }

        List<Rental> rentals = rentalDAO.getRentalsByCustomerId(currentCustomer.getId());
        ObservableList<RentalViewModel> rentalViewModels = FXCollections.observableArrayList();

        for (Rental rental : rentals) {
            Car car = carDAO.getCarById(rental.getCarId());
            if (car != null) {
                RentalViewModel vm = new RentalViewModel(rental, car);
                System.out.println("DIAGNOSTIC: Loaded rental ID " + rental.getId() + " with status: " + vm.getStatus());
                rentalViewModels.add(vm);
            }
        }
        rentalsTable.setItems(rentalViewModels);
    }

    @FXML
    private void handleReturnCar() {
        RentalViewModel selectedRentalVM = rentalsTable.getSelectionModel().getSelectedItem();
        if (selectedRentalVM == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a rental.");
            return;
        }

        String status = selectedRentalVM.getStatus();
        Rental rental = selectedRentalVM.getRental();
        Car car = selectedRentalVM.getCar();

        if ("Pending".equals(status)) {
            // Cancel Request Logic
            rental.setStatus("Cancelled");
            rentalDAO.updateRental(rental);

            // Make car available again
            car.setStatus("Available");
            carDAO.updateCar(car);

            // Refresh global car data service so dashboard updates
            CarDataService.getInstance().refreshAvailableCars();

            showAlert(Alert.AlertType.INFORMATION, "Request Cancelled", "Your rental request has been cancelled.");
        } else if ("Active".equals(status)) {
            // Return Car Logic
            long daysRented = ChronoUnit.DAYS.between(rental.getRentalDate(), LocalDate.now());
            if (daysRented == 0) {
                daysRented = 1; // Minimum 1 day rental
            }
            double finalPrice = daysRented * car.getDailyRate();

            // Update rental
            rental.setReturnDate(LocalDate.now());
            rental.setFinalPrice(finalPrice);
            rental.setStatus("Returned");
            rentalDAO.updateRental(rental);

            // Update car status
            car.setStatus("Available");
            carDAO.updateCar(car);

            // Refresh global car data service so dashboard updates
            CarDataService.getInstance().refreshAvailableCars();

            showAlert(Alert.AlertType.INFORMATION, "Success", 
                String.format("Car '%s %s' returned successfully.\nDays Rented: %d\nTotal Cost: $%.2f", 
                car.getMake(), car.getModel(), daysRented, finalPrice));
        } else {
            showAlert(Alert.AlertType.WARNING, "Invalid Action", "This rental cannot be returned or cancelled.");
            return;
        }

        // Refresh the view
        loadRentals();
        // Reset button state
        returnButton.setDisable(true);
        returnButton.setText("Return Selected Car");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) returnButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
