package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.model.Customer;
import com.example.carrentalmanagementsystem.model.Maintenance;
import com.example.carrentalmanagementsystem.model.Rental;
import com.example.carrentalmanagementsystem.util.NotificationService;
import com.example.carrentalmanagementsystem.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class MainController {

    @FXML
    private BorderPane borderPane;
    @FXML
    private VBox sidebarVBox;
    @FXML
    private Button notificationButton;
    @FXML
    private Label sidebarTitleLabel;

    // Sidebar Buttons
    @FXML private Button dashboardBtn;
    @FXML private Button carsBtn;
    @FXML private Button customersBtn;
    @FXML private Button rentalsBtn;
    @FXML private Button maintenanceBtn;
    @FXML private Button gpsBtn;
    @FXML private Button logoutBtn;
    @FXML private Button exitBtn;

    private DashboardController dashboardController;
    private CarManagementController carManagementController;
    private CustomerManagementController customerManagementController;
    private RentalManagementController rentalManagementController;
    private MaintenanceManagementController maintenanceManagementController;
    private GPSTrackerController gpsTrackerController;

    private boolean sidebarExpanded = false; // Start collapsed
    private Timeline animation;

    @FXML
    public void initialize() {
        System.out.println("DEBUG: MainController initialized.");
        showDashboard();
        applyRoleBasedAccessControl();
        setupNotificationListener();
        
        // Set the initial collapsed state without animation
        setInitialCollapsedState();
    }

    private void setInitialCollapsedState() {
        sidebarVBox.setPrefWidth(60);
        sidebarVBox.setMinWidth(60);
        sidebarVBox.setMaxWidth(60);
        
        sidebarTitleLabel.setText("ABC");
        
        updateButtonState(dashboardBtn, "📊", Pos.CENTER);
        updateButtonState(carsBtn, "🚘", Pos.CENTER);
        updateButtonState(customersBtn, "👥", Pos.CENTER);
        updateButtonState(rentalsBtn, "📋", Pos.CENTER);
        updateButtonState(maintenanceBtn, "🛠", Pos.CENTER);
        updateButtonState(gpsBtn, "📍", Pos.CENTER);
        updateButtonState(logoutBtn, "🔓", Pos.CENTER);
        updateButtonState(exitBtn, "❌", Pos.CENTER);
    }
    
    private void updateButtonState(Button btn, String text, Pos alignment) {
        if (btn != null) {
            btn.setText(text);
            btn.setAlignment(alignment);
        }
    }

    private void setupNotificationListener() {
        NotificationService.getInstance().getPendingRentals().addListener((ListChangeListener<Rental>) c -> {
            Platform.runLater(this::updateNotificationBadge);
        });
        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        int notificationCount = NotificationService.getInstance().getPendingRentals().size();
        if (notificationCount > 0) {
            notificationButton.setText("🔔 " + notificationCount);
            notificationButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffc107; -fx-font-size: 18px; -fx-cursor: hand; -fx-font-weight: bold;");
        } else {
            notificationButton.setText("🔔");
            notificationButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand;");
        }
    }

    @FXML
    private void handleNotificationClick() {
        showRentalManagement();
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

        animation.setOnFinished(event -> {
            sidebarTitleLabel.setText("ABC RENTAL");
            updateButtonState(dashboardBtn, "📊 Dashboard", Pos.CENTER_LEFT);
            updateButtonState(carsBtn, "🚘 Cars", Pos.CENTER_LEFT);
            updateButtonState(customersBtn, "👥 Customers", Pos.CENTER_LEFT);
            updateButtonState(rentalsBtn, "📋 Rentals", Pos.CENTER_LEFT);
            updateButtonState(maintenanceBtn, "🛠 Maintenance", Pos.CENTER_LEFT);
            updateButtonState(gpsBtn, "📍 GPS Tracker", Pos.CENTER_LEFT);
            updateButtonState(logoutBtn, "🔓 Logout", Pos.CENTER_LEFT);
            updateButtonState(exitBtn, "❌ Exit", Pos.CENTER_LEFT);
        });
        animation.play();
    }

    private void collapseSidebar() {
        if (!sidebarExpanded) return;
        sidebarExpanded = false;

        if (animation != null) animation.stop();

        // Change text to icons FIRST to allow shrinking
        sidebarTitleLabel.setText("ABC");
        updateButtonState(dashboardBtn, "📊", Pos.CENTER);
        updateButtonState(carsBtn, "🚘", Pos.CENTER);
        updateButtonState(customersBtn, "👥", Pos.CENTER);
        updateButtonState(rentalsBtn, "📋", Pos.CENTER);
        updateButtonState(maintenanceBtn, "🛠", Pos.CENTER);
        updateButtonState(gpsBtn, "📍", Pos.CENTER);
        updateButtonState(logoutBtn, "🔓", Pos.CENTER);
        updateButtonState(exitBtn, "❌", Pos.CENTER);

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

    private void applyRoleBasedAccessControl() {
        if (SessionManager.isLoggedIn()) {
            if (customersBtn != null) {
                customersBtn.setVisible(false);
                customersBtn.setManaged(false);
            }

            if (SessionManager.isAdmin()) {
                if (maintenanceBtn != null) {
                    maintenanceBtn.setVisible(true);
                    maintenanceBtn.setManaged(true);
                }
            } else if (SessionManager.isStaff()) {
                if (maintenanceBtn != null) {
                    maintenanceBtn.setVisible(false);
                    maintenanceBtn.setManaged(false);
                }
            }
        }
    }

    @FXML
    public void showDashboard() {
        loadView("/com/example/carrentalmanagementsystem/dashboard-view.fxml", "dashboard");
    }

    @FXML
    public void showCarManagement() {
        showCarManagement("All");
    }

    public void showCarManagement(String filter) {
        loadView("/com/example/carrentalmanagementsystem/car-management-view.fxml", "carManagement");
        if (carManagementController != null) {
            carManagementController.applyStatusFilter(filter);
        }
    }

    @FXML
    public void showCustomerManagement() {
        loadView("/com/example/carrentalmanagementsystem/customer-management-view.fxml", "customerManagement");
    }

    @FXML
    public void showRentalManagement() {
        loadView("/com/example/carrentalmanagementsystem/rental-management-view.fxml", "rentalManagement");
    }

    @FXML
    public void showMaintenanceManagement() {
        if (SessionManager.isAdmin()) {
            loadView("/com/example/carrentalmanagementsystem/maintenance-management-view.fxml", "maintenanceManagement");
        }
    }

    @FXML
    public void showGPSTracker() {
        loadView("/com/example/carrentalmanagementsystem/gps-tracker-view.fxml", "gpsTracker");
    }

    @FXML
    private void handleClose() {
        System.exit(0);
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
            } catch (Exception e) {}
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Car Rental System - Select Role");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath, String controllerType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            borderPane.setCenter(view);

            switch (controllerType) {
                case "dashboard":
                    dashboardController = loader.getController();
                    dashboardController.setMainController(this);
                    break;
                case "carManagement":
                    carManagementController = loader.getController();
                    carManagementController.setMainController(this);
                    break;
                case "customerManagement":
                    customerManagementController = loader.getController();
                    customerManagementController.setMainController(this);
                    break;
                case "rentalManagement":
                    rentalManagementController = loader.getController();
                    rentalManagementController.setMainController(this);
                    break;
                case "maintenanceManagement":
                    maintenanceManagementController = loader.getController();
                    maintenanceManagementController.setMainController(this);
                    break;
                case "gpsTracker":
                    gpsTrackerController = loader.getController();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openCarForm(Car carToEdit) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/car-form.fxml"));
            Parent parent = fxmlLoader.load();
            CarFormController carFormController = fxmlLoader.getController();
            carFormController.setMainController(this);

            Stage stage = new Stage();
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {}
            stage.setTitle(carToEdit == null ? "Add New Car" : "Edit Car");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(parent);
            stage.setScene(scene);

            if (carToEdit != null) {
                carFormController.setCar(carToEdit);
            }
            carFormController.setStage(stage);

            stage.showAndWait();
            refreshAllViews();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openCustomerForm(Customer customerToEdit) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/customer-form.fxml"));
            Parent parent = fxmlLoader.load();
            CustomerFormController customerFormController = fxmlLoader.getController();
            customerFormController.setMainController(this);

            Stage stage = new Stage();
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {}
            stage.setTitle(customerToEdit == null ? "Add New Customer" : "Edit Customer");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(parent);
            stage.setScene(scene);

            if (customerToEdit != null) {
                customerFormController.setCustomer(customerToEdit);
            }
            customerFormController.setStage(stage);

            stage.showAndWait();
            refreshAllViews();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openRentalForm(Rental rentalToEdit) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/initiate-rental-view.fxml"));
            Parent parent = fxmlLoader.load();
            
            Stage stage = new Stage();
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {}
            stage.setTitle(rentalToEdit == null ? "Add New Rental" : "Edit Rental");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(parent);
            stage.setScene(scene);

            stage.showAndWait();
            refreshAllViews();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openMaintenanceForm(Maintenance maintenanceToEdit) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/carrentalmanagementsystem/maintenance-form.fxml"));
            Parent parent = fxmlLoader.load();
            MaintenanceFormController maintenanceFormController = fxmlLoader.getController();
            maintenanceFormController.setMainController(this);

            Stage stage = new Stage();
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pic/logo.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {}
            stage.setTitle(maintenanceToEdit == null ? "Add New Maintenance Record" : "Edit Maintenance Record");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(parent);
            stage.setScene(scene);

            if (maintenanceToEdit != null) {
                maintenanceFormController.setMaintenance(maintenanceToEdit);
            }
            maintenanceFormController.setStage(stage);

            stage.showAndWait();
            refreshAllViews();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshAllViews() {
        if (dashboardController != null) {
            dashboardController.refreshDashboard();
        }
        if (carManagementController != null) {
            carManagementController.refreshAll();
        }
        if (rentalManagementController != null) {
            rentalManagementController.refreshTable();
        }
        if (maintenanceManagementController != null) {
            maintenanceManagementController.refreshTable();
        }
    }
}
