package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.CarDAO;
import com.example.carrentalmanagementsystem.dao.RentalDAO;
import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.model.CarLocation;
import com.example.carrentalmanagementsystem.model.Rental;
import com.example.carrentalmanagementsystem.service.GPSTrackingService;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GPSTrackerController {

    @FXML private ComboBox<Rental> activeRentalsComboBox;
    @FXML private Label statusLabel;
    @FXML private Label speedLabel;
    @FXML private Label timestampLabel;
    @FXML private Label coordinatesLabel;
    @FXML private WebView mapWebView;

    private final RentalDAO rentalDAO = new RentalDAO();
    private final CarDAO carDAO = new CarDAO();
    private final GPSTrackingService gpsService = GPSTrackingService.getInstance();
    private WebEngine webEngine;
    private boolean isMapLoaded = false;

    @FXML
    public void initialize() {
        webEngine = mapWebView.getEngine();
        
        // Add listener to know when map is ready
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                isMapLoaded = true;
                System.out.println("DIAGNOSTIC: Map loaded successfully.");
            }
        });

        loadMap();
        loadActiveRentals();

        activeRentalsComboBox.setConverter(new StringConverter<Rental>() {
            @Override
            public String toString(Rental rental) {
                if (rental == null) return null;
                Car car = carDAO.getCarById(rental.getCarId());
                return car != null ? car.getMake() + " " + car.getModel() + " (ID: " + car.getId() + ")" : String.valueOf(rental.getCarId());
            }

            @Override
            public Rental fromString(String string) {
                return null;
            }
        });

        activeRentalsComboBox.setOnAction(e -> updateLocationDisplay());
    }

    public void selectCarById(int carId) {
        // Find the active rental associated with this carId
        Optional<Rental> rentalToSelect = activeRentalsComboBox.getItems().stream()
                .filter(r -> r.getCarId() == carId)
                .findFirst();

        rentalToSelect.ifPresent(rental -> {
            activeRentalsComboBox.setValue(rental);
            updateLocationDisplay();
        });
    }

    private void loadMap() {
        // Load OpenStreetMap via Leaflet (simple HTML wrapper)
        String html = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\" />" +
                "<script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"></script>" +
                "<style>body, html, #map { height: 100%; margin: 0; }</style>" +
                "</head>" +
                "<body>" +
                "<div id=\"map\"></div>" +
                "<script>" +
                "var map = L.map('map').setView([9.03, 38.74], 13);" + // Default to Addis Ababa
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "    attribution: '&copy; OpenStreetMap contributors'" +
                "}).addTo(map);" +
                "var markers = [];" +
                "function clearMarkers() {" +
                "    for(var i=0; i<markers.length; i++) {" +
                "        map.removeLayer(markers[i]);" +
                "    }" +
                "    markers = [];" +
                "}" +
                "function addMarker(lat, lon, title) {" +
                "    var marker = L.marker([lat, lon]).addTo(map)" +
                "        .bindPopup(title);" +
                "    markers.push(marker);" +
                "    return marker;" +
                "}" +
                "function updateMarker(lat, lon, title) {" +
                "    clearMarkers();" +
                "    var marker = addMarker(lat, lon, title);" +
                "    marker.openPopup();" +
                "    map.setView([lat, lon], 15);" +
                "}" +
                "function fitBounds() {" +
                "    if (markers.length > 0) {" +
                "        var group = new L.featureGroup(markers);" +
                "        map.fitBounds(group.getBounds().pad(0.1));" +
                "    }" +
                "}" +
                "</script>" +
                "</body>" +
                "</html>";
        webEngine.loadContent(html);
    }

    private void loadActiveRentals() {
        List<Rental> allRentals = rentalDAO.getAllRentals();
        System.out.println("DIAGNOSTIC: Total rentals in DB: " + allRentals.size());

        List<Rental> activeRentals = allRentals.stream()
                .filter(r -> "Active".equalsIgnoreCase(r.getStatus()) || 
                             (r.getReturnDate() != null && !r.getReturnDate().isBefore(LocalDate.now())))
                .collect(Collectors.toList());
        
        activeRentalsComboBox.getItems().setAll(activeRentals);
        System.out.println("DIAGNOSTIC: Loaded " + activeRentals.size() + " active rentals after filtering.");
    }

    @FXML
    private void handleRefreshLocation() {
        Rental selectedRental = activeRentalsComboBox.getValue();
        if (selectedRental != null) {
            gpsService.updateCarLocation(String.valueOf(selectedRental.getCarId())); 
            updateLocationDisplay();
        }
    }

    @FXML
    private void handleShowAllLocations() {
        System.out.println("DIAGNOSTIC: handleShowAllLocations clicked.");
        
        if (!isMapLoaded) {
            System.out.println("DIAGNOSTIC: Map is not yet loaded. Aborting.");
            return;
        }

        // Clear existing markers
        try {
            webEngine.executeScript("clearMarkers()");
        } catch (Exception e) {
            System.err.println("DIAGNOSTIC: Error clearing markers: " + e.getMessage());
        }
        
        List<Rental> activeRentals = activeRentalsComboBox.getItems();
        if (activeRentals.isEmpty()) {
            System.out.println("DIAGNOSTIC: No active rentals found in ComboBox.");
            return;
        }

        int count = 0;
        for (Rental rental : activeRentals) {
            // In a real app, we would fetch fresh locations for all cars here
            // gpsService.updateCarLocation(String.valueOf(rental.getCarId())); 
            
            CarLocation location = gpsService.getCarLocation(String.valueOf(rental.getCarId()));
            Car car = carDAO.getCarById(rental.getCarId());
            String title = (car != null ? car.getMake() + " " + car.getModel() : "Car " + rental.getCarId()) + 
                           "<br>Speed: " + String.format("%.1f", location.getSpeed()) + " km/h";
            
            // Escape single quotes in title to prevent JS errors
            title = title.replace("'", "\\'");
            
            try {
                webEngine.executeScript("addMarker(" + location.getLatitude() + ", " + location.getLongitude() + ", '" + title + "')");
                count++;
            } catch (Exception e) {
                System.err.println("DIAGNOSTIC: Error adding marker for car " + rental.getCarId() + ": " + e.getMessage());
            }
        }
        
        if (count > 0) {
            try {
                webEngine.executeScript("fitBounds()");
            } catch (Exception e) {
                System.err.println("DIAGNOSTIC: Error fitting bounds: " + e.getMessage());
            }
            statusLabel.setText("Tracking " + count + " cars");
            speedLabel.setText("-");
            timestampLabel.setText(java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            coordinatesLabel.setText("Multiple Locations");
            
            // Clear selection so the user knows they are in "All" mode
            activeRentalsComboBox.setValue(null);
            System.out.println("DIAGNOSTIC: Successfully plotted " + count + " cars.");
        }
    }

    private void updateLocationDisplay() {
        Rental selectedRental = activeRentalsComboBox.getValue();
        if (selectedRental == null) return;

        if (!isMapLoaded) {
             System.out.println("DIAGNOSTIC: Map not loaded yet, skipping updateLocationDisplay.");
             return;
        }

        CarLocation location = gpsService.getCarLocation(String.valueOf(selectedRental.getCarId()));
        
        statusLabel.setText(location.getStatus());
        speedLabel.setText(String.format("%.1f km/h", location.getSpeed()));
        timestampLabel.setText(location.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        coordinatesLabel.setText(String.format("%.4f, %.4f", location.getLatitude(), location.getLongitude()));

        String title = location.getStatus();
        title = title.replace("'", "\\'");

        try {
            webEngine.executeScript("updateMarker(" + location.getLatitude() + ", " + location.getLongitude() + ", '" + title + "')");
        } catch (Exception e) {
            System.err.println("DIAGNOSTIC: Error updating marker: " + e.getMessage());
        }
    }
}
