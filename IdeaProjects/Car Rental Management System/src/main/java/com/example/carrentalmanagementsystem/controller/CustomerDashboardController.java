package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CustomerDAO;
import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.model.Customer;
import com.example.carrentalmanagementsystem.util.CarDataService;
import com.example.carrentalmanagementsystem.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class CustomerDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private TilePane carTilePane;
    
    // Sidebar Elements
    @FXML private VBox sidebarVBox;
    @FXML private Label sidebarTitleLabel;
    @FXML private Button availableCarsBtn;
    @FXML private Button myRentalsBtn;
    @FXML private Button editProfileBtn;
    @FXML private Button logoutBtn;

    private CustomerDAO customerDAO = new CustomerDAO();
    private boolean sidebarExpanded = false;
    private Timeline animation;

    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText("Welcome to ABC car rental , " + SessionManager.getCurrentUser().getUsername() + "!");
        }

        loadCarCards();
        
        CarDataService.getInstance().getAvailableCars().addListener((ListChangeListener<Car>) c -> {
            loadCarCards();
        });
        
        // Set the initial collapsed state without animation
        setInitialCollapsedState();
    }
    
    private void setInitialCollapsedState() {
        sidebarVBox.setPrefWidth(60);
        sidebarVBox.setMinWidth(60);
        sidebarVBox.setMaxWidth(60);
        
        sidebarTitleLabel.setText("ABC");
        
        updateButtonState(availableCarsBtn, "🚗", Pos.CENTER);
        updateButtonState(myRentalsBtn, "📋", Pos.CENTER);
        updateButtonState(editProfileBtn, "👤", Pos.CENTER);
        updateButtonState(logoutBtn, "🔓", Pos.CENTER);
    }
    
    private void updateButtonState(Button btn, String text, Pos alignment) {
        if (btn != null) {
            btn.setText(text);
            btn.setAlignment(alignment);
        }
    }

    @FXML
    private void handleSidebarMouseEntered(MouseEvent event) {
        expandSidebar();
    }

    @FXML
    private void handleSidebarMouseExited(MouseEvent event) {
        collapseSidebar();
    }

    private void expandSidebar() {
        if (sidebarExpanded) return;
        sidebarExpanded = true;

        if (animation != null) animation.stop();
        
        // Set min/max to allow expansion
        sidebarVBox.setMinWidth(VBox.USE_COMPUTED_SIZE);
        sidebarVBox.setMaxWidth(VBox.USE_COMPUTED_SIZE);

        animation = new Timeline();
        KeyValue prefWidthValue = new KeyValue(sidebarVBox.prefWidthProperty(), 240);
        KeyFrame frame = new KeyFrame(Duration.millis(200), prefWidthValue);
        animation.getKeyFrames().add(frame);

        // Update text AFTER the sidebar has expanded
        animation.setOnFinished(event -> {
            sidebarTitleLabel.setText("ABC CAR RENTAL");
            updateButtonState(availableCarsBtn, "🚗 Available Cars", Pos.CENTER_LEFT);
            updateButtonState(myRentalsBtn, "📋 My Rentals", Pos.CENTER_LEFT);
            updateButtonState(editProfileBtn, "👤 Edit Profile", Pos.CENTER_LEFT);
            updateButtonState(logoutBtn, "🔓 Logout", Pos.CENTER_LEFT);
        });
        animation.play();
    }

    private void collapseSidebar() {
        if (!sidebarExpanded) return;
        sidebarExpanded = false;

        if (animation != null) animation.stop();

        // Change text to icons FIRST to allow shrinking
        sidebarTitleLabel.setText("ABC");
        updateButtonState(availableCarsBtn, "🚗", Pos.CENTER);
        updateButtonState(myRentalsBtn, "📋", Pos.CENTER);
        updateButtonState(editProfileBtn, "👤", Pos.CENTER);
        updateButtonState(logoutBtn, "🔓", Pos.CENTER);

        // Animate width
        animation = new Timeline();
        KeyValue prefWidthValue = new KeyValue(sidebarVBox.prefWidthProperty(), 60);
        animation.getKeyFrames().add(new KeyFrame(Duration.millis(200), prefWidthValue));
        
        animation.setOnFinished(event -> {
            // Lock the size after collapsing
            sidebarVBox.setMinWidth(60);
            sidebarVBox.setMaxWidth(60);
        });
        animation.play();
    }

    private void loadCarCards() {
        carTilePane.getChildren().clear();
        for (Car car : CarDataService.getInstance().getAvailableCars()) {
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

    public void openCustomerRentalForm(Car car, ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/customer-initiate-rental-view.fxml"));
            Parent parent = fxmlLoader.load();
            CustomerInitiateRentalController controller = fxmlLoader.getController();
            
            Stage dialogStage = new Stage();
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
                dialogStage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Error loading application icon: " + e.getMessage());
            }
            dialogStage.setTitle("Rent " + car.getMake() + " " + car.getModel());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            
            controller.setCar(car);
            controller.setDialogStage(dialogStage);

            Scene scene = new Scene(parent);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMyRentals(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/customer-rentals-view.fxml"));
            Parent parent = fxmlLoader.load();
            
            Stage stage = new Stage();
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Error loading application icon: " + e.getMessage());
            }
            stage.setTitle("My Rentals");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.showAndWait();
            
            loadCarCards();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/customer-form.fxml"));
            Parent parent = fxmlLoader.load();
            CustomerFormController customerFormController = fxmlLoader.getController();

            Stage stage = new Stage();
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Error loading application icon: " + e.getMessage());
            }
            stage.setTitle("Edit Personal Information");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());

            String currentUsername = SessionManager.getCurrentUser().getUsername();
            Customer currentCustomer = customerDAO.getCustomerByUsername(currentUsername);

            if (currentCustomer != null) {
                customerFormController.setCustomer(currentCustomer);
                customerFormController.hideStatusSection();
            } else {
                System.err.println("Error: Could not retrieve current customer details for editing.");
                return;
            }
            customerFormController.setStage(stage);

            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.showAndWait();

            if (SessionManager.isLoggedIn()) {
                welcomeLabel.setText("Welcome, " + SessionManager.getCurrentUser().getUsername() + "!");
            }
            loadCarCards();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.logout();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/role-selection-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Error loading application icon: " + e.getMessage());
            }
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Car Rental System - Select Role");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
