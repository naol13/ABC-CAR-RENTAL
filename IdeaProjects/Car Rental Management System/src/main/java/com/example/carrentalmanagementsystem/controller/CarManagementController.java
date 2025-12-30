package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CarDAO;
import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.util.CarDataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

public class CarManagementController {

    @FXML
    private TilePane carTilePane;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;

    private CarDAO carDAO;
    private ObservableList<Car> carList;
    private FilteredList<Car> filteredData;
    private MainController mainController; // Reference to MainController

    @FXML
    public void initialize() {
        carDAO = new CarDAO();
        carList = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(carList, p -> true);
        setupFilters();
        refreshAll();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "Available", "Rented", "Under Maintenance"));
        statusFilter.setValue("All");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();

        Predicate<Car> searchPredicate = car -> {
            if (searchText.isEmpty()) {
                return true;
            }
            return (car.getMake() + " " + car.getModel()).toLowerCase().contains(searchText);
        };

        Predicate<Car> statusPredicate = car -> {
            if (status.equals("All")) {
                return true;
            }
            return car.getStatus().equalsIgnoreCase(status);
        };

        filteredData.setPredicate(searchPredicate.and(statusPredicate));
        loadCarCards();
    }

    public void refreshAll() {
        carList.setAll(carDAO.getAllCars());
        System.out.println("DIAGNOSTIC (CarManagementController): Loaded " + carList.size() + " cars from database.");
        loadCarCards();
    }

    private void loadCarCards() {
        carTilePane.getChildren().clear();
        for (Car car : filteredData) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/car-card.fxml"));
                VBox card = loader.load();
                CarCardController controller = loader.getController();
                controller.setData(car, this);
                carTilePane.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAddNewCar() {
        if (mainController != null) {
            mainController.openCarForm(null);
        }
    }

    public void handleEditCar(Car car) {
        if (car != null && mainController != null) {
            mainController.openCarForm(car);
        }
    }

    public void handleDeleteCar(Car car) {
        if (car != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Car: " + car.getMake() + " " + car.getModel());
            alert.setContentText("Are you sure you want to delete this car record?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                carDAO.deleteCar(car.getId());
                
                // Refresh the shared data service
                CarDataService.getInstance().refreshAvailableCars();

                if (mainController != null) {
                    mainController.refreshAllViews();
                } else {
                    refreshAll();
                }
                showAlert(Alert.AlertType.INFORMATION, "Deletion Successful", "Car deleted successfully.");
            }
        }
    }

    public void applyStatusFilter(String status) {
        statusFilter.setValue(status);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
