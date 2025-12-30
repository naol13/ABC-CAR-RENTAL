package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CarDAO;
import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.util.CarDataService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.time.Year; // Import Year for current year validation

public class CarFormController {

    @FXML
    private TextField makeField;
    @FXML
    private TextField modelField;
    @FXML
    private TextField yearField;
    // Removed categoryField
    @FXML
    private ComboBox<String> transmissionComboBox; // Changed from TextField to ComboBox
    @FXML
    private TextField dailyRateField;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private ImageView carImageView;

    private CarDAO carDAO;
    private Car car;
    private MainController mainController;
    private Stage stage;
    private String imagePath;

    @FXML
    public void initialize() {
        carDAO = new CarDAO();
        statusComboBox.setItems(FXCollections.observableArrayList("Available", "Rented", "Under Maintenance"));
        transmissionComboBox.setItems(FXCollections.observableArrayList("Automatic", "Manual")); // Initialize transmission options
    }

    public void setCar(Car car) {
        this.car = car;
        if (car != null) {
            makeField.setText(car.getMake());
            modelField.setText(car.getModel());
            yearField.setText(String.valueOf(car.getYear()));
            // Removed categoryField.setText(car.getCategory());
            transmissionComboBox.setValue(car.getTransmission()); // Set value for ComboBox
            dailyRateField.setText(String.valueOf(car.getDailyRate()));
            statusComboBox.setValue(car.getStatus());
            this.imagePath = car.getImagePath();
            if (this.imagePath != null && !this.imagePath.isEmpty()) {
                try {
                    File file = new File(this.imagePath);
                    if (file.exists()) {
                        carImageView.setImage(new Image(file.toURI().toURL().toString()));
                    } else {
                        carImageView.setImage(null);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    carImageView.setImage(null);
                }
            } else {
                carImageView.setImage(null);
            }
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Car Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            try {
                carImageView.setImage(new Image(selectedFile.toURI().toURL().toString()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Image Error", "Could not load image from selected file.");
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!isInputValid()) {
            return;
        }

        String make = makeField.getText().trim();
        String model = modelField.getText().trim();
        int year = Integer.parseInt(yearField.getText().trim());
        // Removed String category = categoryField.getText();
        String transmission = transmissionComboBox.getValue(); // Get value from ComboBox
        double dailyRate = Double.parseDouble(dailyRateField.getText().trim());
        String status = statusComboBox.getValue();

        if (car == null) {
            // Adjusted Car constructor call
            car = new Car(0, make, model, year, "N/A", transmission, dailyRate, status, imagePath); // "N/A" for category
            carDAO.addCar(car);
        } else {
            car.setMake(make);
            car.setModel(model);
            car.setYear(year);
            // Removed car.setCategory(category);
            car.setTransmission(transmission);
            car.setDailyRate(dailyRate);
            car.setStatus(status);
            car.setImagePath(imagePath);
            carDAO.updateCar(car);
        }

        CarDataService.getInstance().refreshAvailableCars();

        if (mainController != null) {
            mainController.refreshAllViews();
        }
        stage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (makeField.getText().trim().isEmpty()) {
            errorMessage += "No valid make!\n";
        }
        if (modelField.getText().trim().isEmpty()) {
            errorMessage += "No valid model!\n";
        }

        int year = 0;
        try {
            year = Integer.parseInt(yearField.getText().trim());
            int currentYear = Year.now().getValue();
            if (year < 1900 || year > currentYear + 1) { // Allow current year + 1 for new models
                errorMessage += "Year must be between 1900 and " + (currentYear + 1) + "!\n";
            }
        } catch (NumberFormatException e) {
            errorMessage += "No valid year (must be a number)!\n";
        }

        if (transmissionComboBox.getValue() == null || transmissionComboBox.getValue().isEmpty()) {
            errorMessage += "No transmission selected!\n";
        }

        double dailyRate = 0.0;
        try {
            dailyRate = Double.parseDouble(dailyRateField.getText().trim());
            if (dailyRate <= 0) {
                errorMessage += "Daily rate must be a positive number!\n";
            }
        } catch (NumberFormatException e) {
            errorMessage += "No valid daily rate (must be a number)!\n";
        }

        if (statusComboBox.getValue() == null || statusComboBox.getValue().isEmpty()) {
            errorMessage += "No status selected!\n";
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
