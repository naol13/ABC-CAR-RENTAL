package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.model.Car;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;

public class CustomerRentalDetailsController {

    // Removed @FXML private TextField idNumberField;
    @FXML
    private ImageView idImageView;
    @FXML
    private TextField streetField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField stateField;
    // Removed @FXML private TextField postalCodeField;

    private Car selectedCar;
    private LocalDate startDate;
    private LocalDate endDate;
    private String idImagePath;

    // Method to receive data from the previous step
    public void setRentalData(Car car, LocalDate start, LocalDate end) {
        this.selectedCar = car;
        this.startDate = start;
        this.endDate = end;
        // Pre-fill any customer details if available from SessionManager or CustomerDAO
        // For now, we'll leave it blank
    }

    // Method to pre-fill details when navigating back
    public void prefillDetails(String idImgPath, String str, String cty, String st) { // Removed idNum, pc
        idImagePath = idImgPath;
        if (idImgPath != null && !idImgPath.isEmpty()) {
            try {
                idImageView.setImage(new Image(new File(idImgPath).toURI().toURL().toString()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                idImageView.setImage(null);
            }
        }
        streetField.setText(str);
        cityField.setText(cty);
        stateField.setText(st);
    }

    @FXML
    private void handleBrowseIdImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select ID Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        // Changed to use streetField to get the window, as idNumberField is removed
        Stage stage = (Stage) streetField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            idImagePath = selectedFile.getAbsolutePath();
            try {
                idImageView.setImage(new Image(selectedFile.toURI().toURL().toString()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Image Error", "Could not load image from selected file.");
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/customer-initiate-rental-view.fxml"));
            Parent root = fxmlLoader.load();
            CustomerInitiateRentalController controller = fxmlLoader.getController();
            // Pass data back to the previous controller
            controller.setCar(selectedCar); // Pass car back
            controller.setDates(startDate, endDate); // Pass dates back
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Initiate Your Rental");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not go back to previous step.");
        }
    }

    @FXML
    private void handleNext(ActionEvent event) {
        if (!isInputValid()) {
            return;
        }

        // Collect all data for the next step
        // Removed String idNumber = idNumberField.getText();
        String street = streetField.getText();
        String city = cityField.getText();
        String state = stateField.getText();
        // Removed String postalCode = postalCodeField.getText();

        // Navigate to the Confirmation step
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/customer-rental-confirmation-view.fxml"));
            Parent root = fxmlLoader.load();
            CustomerRentalConfirmationController controller = fxmlLoader.getController();
            
            // Pass all collected data to the confirmation controller
            // Adjusted setRentalData call
            controller.setRentalData(selectedCar, startDate, endDate, idImagePath, street, city, state);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Confirm Your Rental");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not proceed to final confirmation.");
        }
    }

    private boolean isInputValid() {
        String errorMessage = "";

        // Removed validation for idNumberField
        if (idImagePath == null || idImagePath.isEmpty()) {
            errorMessage += "ID picture is required!\n";
        }
        if (streetField.getText() == null || streetField.getText().isEmpty()) {
            errorMessage += "Street address is required!\n";
        }
        if (cityField.getText() == null || cityField.getText().isEmpty()) {
            errorMessage += "City is required!\n";
        }
        if (stateField.getText() == null || stateField.getText().isEmpty()) {
            errorMessage += "State/Province is required!\n";
        }
        // Removed validation for postalCodeField

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Invalid Fields", errorMessage);
            return false;
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
