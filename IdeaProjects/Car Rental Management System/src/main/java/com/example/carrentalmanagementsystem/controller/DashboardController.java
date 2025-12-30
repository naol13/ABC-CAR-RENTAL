package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CarDAO;
import com.example.carrentalmanagementsystem.dao.CustomerDAO;
import com.example.carrentalmanagementsystem.dao.RentalDAO;
import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.model.Customer;
import com.example.carrentalmanagementsystem.model.Rental;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML
    private Label totalCarsLabel;
    @FXML
    private Label availableCarsLabel;
    @FXML
    private Label activeRentalsLabel;
    @FXML
    private Label totalCustomersLabel;
    @FXML
    private BarChart<String, Number> weeklyPerformanceChart;
    @FXML
    private PieChart carStatusPieChart;
    @FXML
    private TableView<RentalActivityDisplay> recentActivitiesTable;
    @FXML
    private ListView<String> overdueRentalsListView; // Added ListView
    @FXML
    private Button addNewCarButton;
    @FXML
    private Button addNewCustomerButton;

    private final CarDAO carDAO = new CarDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final RentalDAO rentalDAO = new RentalDAO();
    private MainController mainController;

    @FXML
    public void initialize() {
        refreshDashboard();
        setupQuickActions();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void refreshDashboard() {
        loadDashboardMetrics();
        populateWeeklyPerformanceChart();
        populateCarStatusPieChart();
        populateRecentActivitiesTable();
        populateOverdueRentals(); // Added call
    }

    private void loadDashboardMetrics() {
        List<Car> cars = carDAO.getAllCars();
        long availableCars = cars.stream().filter(Car::isAvailable).count();
        totalCarsLabel.setText(String.valueOf(cars.size()));
        availableCarsLabel.setText(String.valueOf(availableCars));

        List<Customer> customers = customerDAO.getAllCustomers();
        totalCustomersLabel.setText(String.valueOf(customers.size()));

        List<Rental> rentals = rentalDAO.getAllRentals();
        long activeRentals = rentals.stream().filter(r -> r.getReturnDate() == null || r.getReturnDate().isAfter(LocalDate.now())).count();
        activeRentalsLabel.setText(String.valueOf(activeRentals));
    }

    private void populateWeeklyPerformanceChart() {
        weeklyPerformanceChart.getData().clear();
        List<Rental> rentals = rentalDAO.getAllRentals();
        
        Map<String, Long> rentalsByDay = rentals.stream()
                .filter(r -> r.getRentalDate().isAfter(LocalDate.now().minusDays(7)))
                .collect(Collectors.groupingBy(
                        r -> r.getRentalDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Rentals");

        LocalDate date = LocalDate.now().minusDays(6);
        for (int i = 0; i < 7; i++) {
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            long count = rentalsByDay.getOrDefault(dayName, 0L);
            XYChart.Data<String, Number> data = new XYChart.Data<>(dayName, count);
            series.getData().add(data);
            date = date.plusDays(1);
        }

        weeklyPerformanceChart.getData().add(series);
    }

    private void populateCarStatusPieChart() {
        carStatusPieChart.getData().clear();
        List<Car> cars = carDAO.getAllCars();
        long available = cars.stream().filter(Car::isAvailable).count();
        long rented = cars.size() - available;

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Available", available),
                new PieChart.Data("Rented", rented)
        );

        carStatusPieChart.setData(pieChartData);
    }

    private void populateRecentActivitiesTable() {
        recentActivitiesTable.getColumns().clear();

        TableColumn<RentalActivityDisplay, String> carColumn = new TableColumn<>("Car");
        carColumn.setCellValueFactory(new PropertyValueFactory<>("carMake"));

        TableColumn<RentalActivityDisplay, String> customerColumn = new TableColumn<>("Customer");
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<RentalActivityDisplay, String> contactColumn = new TableColumn<>("Contact");
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("customerContact"));

        TableColumn<RentalActivityDisplay, LocalDate> rentalDateColumn = new TableColumn<>("Rental Date");
        rentalDateColumn.setCellValueFactory(new PropertyValueFactory<>("rentalDate"));

        recentActivitiesTable.getColumns().addAll(carColumn, customerColumn, contactColumn, rentalDateColumn);

        List<Rental> rentals = rentalDAO.getAllRentals();
        ObservableList<RentalActivityDisplay> recentActivities = FXCollections.observableArrayList();
        for (int i = 0; i < 5 && i < rentals.size(); i++) {
            Rental rental = rentals.get(rentals.size() - 1 - i);
            Car car = carDAO.getCarById(rental.getCarId());
            Customer customer = customerDAO.getCustomerById(rental.getCustomerId());

            if (car != null && customer != null) {
                recentActivities.add(new RentalActivityDisplay(
                        car.getMake() + " " + car.getModel(),
                        customer.getName(),
                        customer.getContact(),
                        rental.getRentalDate()
                ));
            }
        }
        recentActivitiesTable.setItems(recentActivities);
    }

    private void populateOverdueRentals() {
        List<Rental> overdueRentals = rentalDAO.getOverdueRentals();
        ObservableList<String> items = FXCollections.observableArrayList();

        if (overdueRentals.isEmpty()) {
            items.add("No overdue rentals.");
        } else {
            for (Rental rental : overdueRentals) {
                Car car = carDAO.getCarById(rental.getCarId());
                Customer customer = customerDAO.getCustomerById(rental.getCustomerId());
                if (car != null && customer != null) {
                    items.add(String.format("%s - %s (Due: %s)", 
                            car.getModel(), 
                            customer.getName(), 
                            rental.getReturnDate()));
                }
            }
        }
        overdueRentalsListView.setItems(items);
    }

    private void setupQuickActions() {
        addNewCarButton.setOnAction(event -> {
            if (mainController != null) {
                mainController.openCarForm(null);
            }
        });

        addNewCustomerButton.setOnAction(event -> {
            if (mainController != null) {
                mainController.openCustomerForm(null);
            }
        });
    }

    @FXML
    private void handleTotalCarsClick(MouseEvent event) {
        if (mainController != null) {
            mainController.showCarManagement("All");
        }
    }

    @FXML
    private void handleAvailableCarsClick(MouseEvent event) {
        if (mainController != null) {
            mainController.showCarManagement("Available");
        }
    }

    @FXML
    private void handleActiveRentalsClick(MouseEvent event) {
        if (mainController != null) {
            mainController.showRentalManagement();
        }
    }

    @FXML
    private void handleTotalCustomersClick(MouseEvent event) {
        if (mainController != null) {
            mainController.showCustomerManagement();
        }
    }

    public static class RentalActivityDisplay {
        private final String carMake;
        private final String customerName;
        private final String customerContact;
        private final LocalDate rentalDate;

        public RentalActivityDisplay(String carMake, String customerName, String customerContact, LocalDate rentalDate) {
            this.carMake = carMake;
            this.customerName = customerName;
            this.customerContact = customerContact;
            this.rentalDate = rentalDate;
        }

        public String getCarMake() {
            return carMake;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getCustomerContact() {
            return customerContact;
        }

        public LocalDate getRentalDate() {
            return rentalDate;
        }
    }
}
