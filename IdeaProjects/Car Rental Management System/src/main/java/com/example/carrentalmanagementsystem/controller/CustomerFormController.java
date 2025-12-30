package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CarDAO;
import com.example.carrentalmanagementsystem.dao.CustomerDAO;
import com.example.carrentalmanagementsystem.dao.RentalDAO;
import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.model.Customer;
import com.example.carrentalmanagementsystem.model.Rental;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class CustomerFormController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField driversLicenseField;
    @FXML
    private TextField contactField;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private PasswordField passwordField;
    // Removed birthDatePicker

    @FXML
    private VBox rentalHistorySection;
    @FXML
    private TableView<RentalDisplay> rentalHistoryTable;
    @FXML
    private TableColumn<RentalDisplay, String> carColumn;
    @FXML
    private TableColumn<RentalDisplay, LocalDate> rentalDateColumn;
    @FXML
    private TableColumn<RentalDisplay, LocalDate> returnDateColumn;
    @FXML
    private TableColumn<RentalDisplay, Double> totalCostColumn;
    @FXML
    private TableColumn<RentalDisplay, Void> trackButtonColumn;

    private CustomerDAO customerDAO;
    private RentalDAO rentalDAO;
    private CarDAO carDAO;
    private Customer customer;
    private MainController mainController;
    private CustomerLoginController loginController;
    private Stage stage;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
            "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

    private static final String NAME_PATTERN = "^[a-zA-Z\\s]+$";
    private static final Pattern namePattern = Pattern.compile(NAME_PATTERN);

    private static final String PHONE_PATTERN = "^\\d{10}$";
    private static final Pattern phonePattern = Pattern.compile(PHONE_PATTERN);

    @FXML
    public void initialize() {
        customerDAO = new CustomerDAO();
        rentalDAO = new RentalDAO();
        carDAO = new CarDAO();

        statusComboBox.setItems(FXCollections.observableArrayList("Active", "Inactive", "New"));
        statusComboBox.setVisible(false);
        statusComboBox.setManaged(false);

        if (rentalHistorySection != null) {
            rentalHistorySection.setVisible(false);
            rentalHistorySection.setManaged(false);
        }

        carColumn.setCellValueFactory(new PropertyValueFactory<>("carDetails"));
        rentalDateColumn.setCellValueFactory(new PropertyValueFactory<>("rentalDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        setupTrackButtonColumn();
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            nameField.setText(customer.getName());
            emailField.setText(customer.getEmail());
            driversLicenseField.setText(customer.getDriversLicense());
            contactField.setText(customer.getContact());
            passwordField.setText(customer.getPassword());
            // Removed birthDatePicker.setValue(customer.getBirthDate());

            statusComboBox.setValue(customer.getStatus());
            statusComboBox.setVisible(true);
            statusComboBox.setManaged(true);

            if (rentalHistorySection != null) {
                rentalHistorySection.setVisible(true);
                rentalHistorySection.setManaged(true);
                loadRentalHistory(customer.getId());
            }
        } else {
            if (rentalHistorySection != null) {
                rentalHistorySection.setVisible(false);
                rentalHistorySection.setManaged(false);
            }
        }
    }

    private void loadRentalHistory(int customerId) {
        ObservableList<RentalDisplay> rentalDisplays = FXCollections.observableArrayList();
        List<Rental> rentals = rentalDAO.getRentalsByCustomerId(customerId);

        for (Rental rental : rentals) {
            Car car = carDAO.getCarById(rental.getCarId());
            if (car != null) {
                rentalDisplays.add(new RentalDisplay(
                        rental.getCarId(),
                        car.getMake() + " " + car.getModel() + " (" + car.getYear() + ")",
                        rental.getRentalDate(),
                        rental.getReturnDate(),
                        rental.getFinalPrice()
                ));
            }
        }
        rentalHistoryTable.setItems(rentalDisplays);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setLoginController(CustomerLoginController loginController) {
        this.loginController = loginController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void hideStatusSection() {
        if (statusComboBox != null) {
            statusComboBox.setVisible(false);
            statusComboBox.setManaged(false);
            if (statusComboBox.getParent() instanceof javafx.scene.layout.GridPane) {
                javafx.scene.layout.GridPane parentGrid = (javafx.scene.layout.GridPane) statusComboBox.getParent();
                for (javafx.scene.Node node : parentGrid.getChildren()) {
                    if (node instanceof javafx.scene.control.Label &&
                        javafx.scene.layout.GridPane.getRowIndex(node) == javafx.scene.layout.GridPane.getRowIndex(statusComboBox) &&
                        javafx.scene.layout.GridPane.getColumnIndex(node) == 0) {
                        node.setVisible(false);
                        node.setManaged(false);
                        break;
                    }
                }
            }
        }
    }

    public void showStatusSection() {
        if (statusComboBox != null) {
            statusComboBox.setVisible(true);
            statusComboBox.setManaged(true);
            if (statusComboBox.getParent() instanceof javafx.scene.layout.GridPane) {
                javafx.scene.layout.GridPane parentGrid = (javafx.scene.layout.GridPane) statusComboBox.getParent();
                for (javafx.scene.Node node : parentGrid.getChildren()) {
                    if (node instanceof javafx.scene.control.Label &&
                        javafx.scene.layout.GridPane.getRowIndex(node) == javafx.scene.layout.GridPane.getRowIndex(statusComboBox) &&
                        javafx.scene.layout.GridPane.getColumnIndex(node) == 0) {
                        node.setVisible(true);
                        node.setManaged(true);
                        break;
                    }
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!isInputValid()) {
            return;
        }

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String driversLicense = driversLicenseField.getText().trim();
        String contact = contactField.getText().trim();
        String password = passwordField.getText().trim();
        LocalDate birthDate = null; // Set birthDate to null
        
        String status = (statusComboBox.isVisible() && statusComboBox.getValue() != null) ? statusComboBox.getValue() : "New";

        if (customer == null) {
            customer = new Customer(0, name, email, driversLicense, contact, 0, null, status, email, password, birthDate); // Use email as username
            boolean success = customerDAO.addCustomer(customer);

            if (success) {
                if (loginController != null) {
                    stage.close();
                    loginController.loginAndNavigate(customer, stage);
                } else if (mainController != null) {
                    mainController.refreshAllViews();
                    stage.close();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Registration Failed");
                alert.setHeaderText("Could not register customer");
                alert.setContentText("A customer with this email, driver's license, or username likely already exists.");
                alert.showAndWait();
                // Reset customer to null so user can try again
                customer = null;
            }
        } else {
            customer.setName(name);
            customer.setEmail(email);
            customer.setDriversLicense(driversLicense);
            customer.setContact(contact);
            customer.setStatus(status);
            customer.setUsername(email); // Use email as username
            customer.setPassword(password);
            customer.setBirthDate(birthDate);
            customerDAO.updateCustomerUnsafe(customer);
            
            if (mainController != null) {
                mainController.refreshAllViews();
            }
            stage.close();
        }
    }

    private boolean isInputValid() {
        String errorMessage = "";

        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            errorMessage += "No valid name!\n";
        } else if (!namePattern.matcher(name).matches()) {
            errorMessage += "Name can only contain letters and spaces!\n";
        }

        String email = emailField.getText();
        if (email == null || email.trim().isEmpty()) {
            errorMessage += "No valid email!\n";
        } else if (!emailPattern.matcher(email).matches()) {
            errorMessage += "Invalid email format!\n";
        }
        if (driversLicenseField.getText() == null || driversLicenseField.getText().trim().isEmpty()) {
            errorMessage += "No valid driver's license!\n";
        }
        
        String contact = contactField.getText();
        if (contact == null || contact.trim().isEmpty()) {
            errorMessage += "No valid contact info!\n";
        } else if (!phonePattern.matcher(contact).matches()) {
            errorMessage += "Contact number must be exactly 10 digits!\n";
        }

        if (passwordField.getText() == null || passwordField.getText().trim().isEmpty()) {
            errorMessage += "No valid password!\n";
        }

        // Removed birthDate validation

        if (statusComboBox.isVisible() && (statusComboBox.getValue() == null || statusComboBox.getValue().isEmpty())) {
            errorMessage += "No valid status!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(stage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }

    private void setupTrackButtonColumn() {
        Callback<TableColumn<RentalDisplay, Void>, TableCell<RentalDisplay, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<RentalDisplay, Void> call(final TableColumn<RentalDisplay, Void> param) {
                final TableCell<RentalDisplay, Void> cell = new TableCell<>() {
                    private final Button btn = new Button("📍");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            RentalDisplay data = getTableView().getItems().get(getIndex());
                            openGpsTracker(data.getCarId());
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };
        trackButtonColumn.setCellFactory(cellFactory);
    }

    private void openGpsTracker(int carId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/gps-tracker-view.fxml"));
            Parent root = loader.load();
            
            GPSTrackerController controller = loader.getController();
            controller.selectCarById(carId);

            Stage gpsStage = new Stage();
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
                gpsStage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Error loading application icon for GPS tracker: " + e.getMessage());
            }
            gpsStage.setTitle("GPS Tracker - Car ID: " + carId);
            gpsStage.initModality(Modality.WINDOW_MODAL);
            gpsStage.initOwner(stage);
            Scene scene = new Scene(root);
            gpsStage.setScene(scene);
            gpsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class RentalDisplay {
        private final int carId;
        private final String carDetails;
        private final LocalDate rentalDate;
        private final LocalDate returnDate;
        private final Double totalCost;

        public RentalDisplay(int carId, String carDetails, LocalDate rentalDate, LocalDate returnDate, Double totalCost) {
            this.carId = carId;
            this.carDetails = carDetails;
            this.rentalDate = rentalDate;
            this.returnDate = returnDate;
            this.totalCost = totalCost;
        }

        public int getCarId() {
            return carId;
        }

        public String getCarDetails() {
            return carDetails;
        }

        public LocalDate getRentalDate() {
            return rentalDate;
        }

        public LocalDate getReturnDate() {
            return returnDate;
        }

        public Double getTotalCost() {
            return totalCost;
        }
    }
}
