package com.example.carrentalmanagementsystem.model;

import java.time.LocalDate;

public class Rental {
    private int id;
    private int carId;
    private int customerId;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private double finalPrice;
    private String status; // "Pending", "Active", "Completed"

    public Rental(int id, int carId, int customerId, LocalDate rentalDate, LocalDate returnDate, double finalPrice, String status) {
        this.id = id;
        this.carId = carId;
        this.customerId = customerId;
        this.rentalDate = rentalDate;
        this.returnDate = returnDate;
        this.finalPrice = finalPrice;
        this.status = status;
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

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public LocalDate getRentalDate() {
        return rentalDate;
    }

    public void setRentalDate(LocalDate rentalDate) {
        this.rentalDate = rentalDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
