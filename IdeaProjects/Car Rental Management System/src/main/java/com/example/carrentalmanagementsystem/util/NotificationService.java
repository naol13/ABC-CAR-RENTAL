package com.example.carrentalmanagementsystem.util;

import com.example.carrentalmanagementsystem.model.Rental;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NotificationService {
    private static NotificationService instance;
    private final ObservableList<Rental> pendingRentals = FXCollections.observableArrayList();

    private NotificationService() {}

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public void addRentalRequest(Rental rental) {
        pendingRentals.add(rental);
    }

    public void removeRentalRequest(Rental rental) {
        pendingRentals.remove(rental);
    }

    public ObservableList<Rental> getPendingRentals() {
        return pendingRentals;
    }

    public void clearAllRequests() {
        pendingRentals.clear();
    }
}
