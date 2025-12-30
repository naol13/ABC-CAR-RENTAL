package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CarDAO;
import com.example.carrentalmanagementsystem.dao.CustomerDAO;
import com.example.carrentalmanagementsystem.dao.RentalDAO;
import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.model.Customer;
import com.example.carrentalmanagementsystem.model.Rental;
import com.example.carrentalmanagementsystem.util.NotificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RentalManagementController {

    @FXML
    private TableView<RentalInfoDisplay> rentalTableView;
    @FXML
    private TableColumn<RentalInfoDisplay, String> carInfoColumn;
    @FXML
    private TableColumn<RentalInfoDisplay, String> customerInfoColumn;
    @FXML
    private TableColumn<RentalInfoDisplay, String> contactColumn;
    @FXML
    private TableColumn<RentalInfoDisplay, String> licenseColumn;
    @FXML
    private TableColumn<RentalInfoDisplay, LocalDate> rentalDateColumn;
    @FXML
    private TableColumn<RentalInfoDisplay, LocalDate> returnDateColumn;
    @FXML
    private TableColumn<RentalInfoDisplay, String> statusColumn;
    @FXML
    private TableColumn<RentalInfoDisplay, Void> actionColumn;

    private RentalDAO rentalDAO;
    private CarDAO carDAO;
    private CustomerDAO customerDAO;
    private MainController mainController;

    @FXML
    public void initialize() {
        rentalDAO = new RentalDAO();
        carDAO = new CarDAO();
        customerDAO = new CustomerDAO();
        setupTableColumns();
        refreshTable();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setupTableColumns() {
        carInfoColumn.setCellValueFactory(new PropertyValueFactory<>("carInfo"));
        customerInfoColumn.setCellValueFactory(new PropertyValueFactory<>("customerInfo"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("customerContact"));
        licenseColumn.setCellValueFactory(new PropertyValueFactory<>("customerLicense"));
        rentalDateColumn.setCellValueFactory(new PropertyValueFactory<>("rentalDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        addActionButtonsToTable();
    }

    public void refreshTable() {
        List<Rental> allRentals = rentalDAO.getAllRentals();
        List<RentalInfoDisplay> displayList = new ArrayList<>();

        for (Rental rental : allRentals) {
            Car car = carDAO.getCarById(rental.getCarId());
            Customer customer = customerDAO.getCustomerById(rental.getCustomerId());
            
            String carInfo = (car != null) ? car.getMake() + " " + car.getModel() : "Unknown Car (ID: " + rental.getCarId() + ")";
            String customerInfo = (customer != null) ? customer.getName() : "Unknown Customer (ID: " + rental.getCustomerId() + ")";
            String customerContact = (customer != null) ? customer.getContact() : "N/A";
            String customerLicense = (customer != null) ? customer.getDriversLicense() : "N/A";

            displayList.add(new RentalInfoDisplay(rental, carInfo, customerInfo, customerContact, customerLicense));
        }

        ObservableList<RentalInfoDisplay> observableList = FXCollections.observableArrayList(displayList);
        rentalTableView.setItems(observableList);
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<RentalInfoDisplay, Void>, TableCell<RentalInfoDisplay, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<RentalInfoDisplay, Void> call(final TableColumn<RentalInfoDisplay, Void> param) {
                return new TableCell<>() {
                    private final Button approveButton = new Button("Approve");
                    private final Button returnButton = new Button("Return");
                    private final HBox pane = new HBox(5);

                    {
                        approveButton.setOnAction((ActionEvent event) -> {
                            RentalInfoDisplay displayItem = getTableView().getItems().get(getIndex());
                            handleApprove(displayItem.getRental());
                        });
                        returnButton.setOnAction((ActionEvent event) -> {
                            RentalInfoDisplay displayItem = getTableView().getItems().get(getIndex());
                            handleReturn(displayItem.getRental());
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            RentalInfoDisplay displayItem = getTableView().getItems().get(getIndex());
                            pane.getChildren().clear();
                            if ("Pending".equalsIgnoreCase(displayItem.getStatus())) {
                                pane.getChildren().add(approveButton);
                            } else if ("Active".equalsIgnoreCase(displayItem.getStatus())) {
                                pane.getChildren().add(returnButton);
                            }
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }

    private void handleApprove(Rental rental) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Approval");
        alert.setHeaderText("Approve Rental Request");
        alert.setContentText("Are you sure you want to approve this rental?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            rental.setStatus("Active");
            rentalDAO.updateRental(rental);

            Car rentedCar = carDAO.getCarById(rental.getCarId());
            if (rentedCar != null) {
                rentedCar.setStatus("Rented");
                carDAO.updateCar(rentedCar);
            }

            NotificationService.getInstance().removeRentalRequest(rental);
            refreshTable();
            if (mainController != null) {
                mainController.refreshAllViews();
            }
        }
    }

    private void handleReturn(Rental rental) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Return");
        alert.setHeaderText("Return Car");
        alert.setContentText("Are you sure you want to mark this car as returned?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            rental.setReturnDate(LocalDate.now());
            rental.setStatus("Completed");
            rentalDAO.updateRental(rental);

            Car rentedCar = carDAO.getCarById(rental.getCarId());
            if (rentedCar != null) {
                rentedCar.setStatus("Available");
                carDAO.updateCar(rentedCar);
            }

            refreshTable();
            if (mainController != null) {
                mainController.refreshAllViews();
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

    // Inner class to wrap Rental data with display strings
    public static class RentalInfoDisplay {
        private final Rental rental;
        private final String carInfo;
        private final String customerInfo;
        private final String customerContact;
        private final String customerLicense;

        public RentalInfoDisplay(Rental rental, String carInfo, String customerInfo, String customerContact, String customerLicense) {
            this.rental = rental;
            this.carInfo = carInfo;
            this.customerInfo = customerInfo;
            this.customerContact = customerContact;
            this.customerLicense = customerLicense;
        }

        public Rental getRental() {
            return rental;
        }

        public String getCarInfo() {
            return carInfo;
        }

        public String getCustomerInfo() {
            return customerInfo;
        }

        public String getCustomerContact() {
            return customerContact;
        }

        public String getCustomerLicense() {
            return customerLicense;
        }

        public LocalDate getRentalDate() {
            return rental.getRentalDate();
        }

        public LocalDate getReturnDate() {
            return rental.getReturnDate();
        }

        public String getStatus() {
            return rental.getStatus();
        }
    }
}
