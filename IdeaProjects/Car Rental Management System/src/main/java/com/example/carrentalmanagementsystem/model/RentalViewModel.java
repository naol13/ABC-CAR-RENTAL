package com.example.carrentalmanagementsystem.model;

import java.time.LocalDate;

public class RentalViewModel {
    private int rentalId;
    private String carMake;
    private String carModel;
    private LocalDate rentalDate;
    private double dailyRate;
    private String status;
    private Rental rental; // Keep reference to original rental object
    private Car car; // Keep reference to original car object

    public RentalViewModel(Rental rental, Car car) {
        this.rental = rental;
        this.car = car;
        this.rentalId = rental.getId();
        this.carMake = car.getMake();
        this.carModel = car.getModel();
        this.rentalDate = rental.getRentalDate();
        this.dailyRate = car.getDailyRate();
        // Use the actual status from the rental object
        this.status = rental.getStatus();
    }

    public int getRentalId() { return rentalId; }
    public String getCarMake() { return carMake; }
    public String getCarModel() { return carModel; }
    public LocalDate getRentalDate() { return rentalDate; }
    public double getDailyRate() { return dailyRate; }
    public String getStatus() { return status; }
    public Rental getRental() { return rental; }
    public Car getCar() { return car; }
}
