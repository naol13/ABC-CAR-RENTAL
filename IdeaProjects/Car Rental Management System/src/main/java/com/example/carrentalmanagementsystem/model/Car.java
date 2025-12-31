package com.example.carrentalmanagementsystem.model;

public class Car {
    private int id;
    private String make;
    private String model;
    private int year;
    private String category;
    private String transmission;
    private double dailyRate;
    private String status; // "Available", "Rented", "Under Maintenance"
    private String imagePath; // Added for car picture functionality

    public Car(int id, String make, String model, int year, String category, String transmission, double dailyRate, String status, String imagePath) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.year = year;
        this.category = category;
        this.transmission = transmission;
        this.dailyRate = dailyRate;
        this.status = status;
        this.imagePath = imagePath;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(status);
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
