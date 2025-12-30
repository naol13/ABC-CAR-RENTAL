package com.example.carrentalmanagementsystem.util;

import com.example.carrentalmanagementsystem.dao.CarDAO;
import com.example.carrentalmanagementsystem.model.Car;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CarDataService {
    private static CarDataService instance;
    private final CarDAO carDAO = new CarDAO();
    private final ObservableList<Car> availableCars = FXCollections.observableArrayList();

    private CarDataService() {
        refreshAvailableCars();
    }

    public static synchronized CarDataService getInstance() {
        if (instance == null) {
            instance = new CarDataService();
        }
        return instance;
    }

    public ObservableList<Car> getAvailableCars() {
        return availableCars;
    }

    public void refreshAvailableCars() {
        availableCars.setAll(carDAO.getAvailableCars());
    }
}
