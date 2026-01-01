package com.example.carrentalmanagementsystem.model;

import java.time.LocalDateTime;

public class CarLocation {
    private String carId;
    private double latitude;
    private double longitude;
    private double speed; // km/h
    private LocalDateTime timestamp;
    private String status; // "Moving", "Stopped", "Parked"

    public CarLocation(String carId, double latitude, double longitude, double speed, String status) {
        this.carId = carId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public String getCarId() { return carId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getSpeed() { return speed; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getStatus() { return status; }

    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
