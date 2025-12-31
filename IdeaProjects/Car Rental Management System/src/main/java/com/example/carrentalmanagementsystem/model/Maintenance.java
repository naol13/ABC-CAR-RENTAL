package com.example.carrentalmanagementsystem.model;

import java.time.LocalDate;

public class Maintenance {
    private int id;
    private int carId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private double cost;

    public Maintenance(int id, int carId, LocalDate startDate, LocalDate endDate, String description, double cost) {
        this.id = id;
        this.carId = carId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.cost = cost;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
