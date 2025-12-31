package com.example.carrentalmanagementsystem.model;

import java.time.LocalDate;

public class Customer {
    private int id;
    private String name;
    private String email;
    private String driversLicense;
    private String contact;
    private int totalRentals;
    private LocalDate lastRentalDate;
    private String status;
    private String username;
    private String password;
    private LocalDate birthDate; // New field for birth date

    public Customer(int id, String name, String email, String driversLicense, String contact, int totalRentals, LocalDate lastRentalDate, String status, String username, String password, LocalDate birthDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.driversLicense = driversLicense;
        this.contact = contact;
        this.totalRentals = totalRentals;
        this.lastRentalDate = lastRentalDate;
        this.status = status;
        this.username = username;
        this.password = password;
        this.birthDate = birthDate; // Initialize new field
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDriversLicense() {
        return driversLicense;
    }

    public void setDriversLicense(String driversLicense) {
        this.driversLicense = driversLicense;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public int getTotalRentals() {
        return totalRentals;
    }

    public void setTotalRentals(int totalRentals) {
        this.totalRentals = totalRentals;
    }

    public LocalDate getLastRentalDate() {
        return lastRentalDate;
    }

    public void setLastRentalDate(LocalDate lastRentalDate) {
        this.lastRentalDate = lastRentalDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
