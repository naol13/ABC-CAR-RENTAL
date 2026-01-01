package com.example.carrentalmanagementsystem.dao;

import com.example.carrentalmanagementsystem.model.Car;
import com.example.carrentalmanagementsystem.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarDAO {

    public CarDAO() {
        // The createTable logic is now handled by DatabaseConnection.initializeDatabase()
    }

    public void addCar(Car car) {
        // The SQL statement is correct, as the trigger will handle the 'id' column.
        String sql = "INSERT INTO cars(make, model, year, category, transmission, daily_rate, status, imagePath) VALUES(?,?,?,?,?,?,?,?)";
        
        // We need to retrieve the generated ID, so we specify the column name.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"id"})) {
            
            pstmt.setString(1, car.getMake());
            pstmt.setString(2, car.getModel());
            pstmt.setInt(3, car.getYear());
            pstmt.setString(4, car.getCategory());
            pstmt.setString(5, car.getTransmission());
            pstmt.setDouble(6, car.getDailyRate());
            pstmt.setString(7, car.getStatus());
            pstmt.setString(8, car.getImagePath());
            
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Retrieve the generated ID and set it on the car object
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        car.setId(generatedKeys.getInt(1));
                        System.out.println("DIAGNOSTIC (CarDAO): Car inserted with ID: " + car.getId());
                    }
                }
            }

        } catch (SQLException e) {
            // Add detailed error logging
            System.err.println("ERROR (CarDAO): Failed to add car: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Car getCarById(int carId) {
        String sql = "SELECT * FROM cars WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, carId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Car(
                            rs.getInt("id"),
                            rs.getString("make"),
                            rs.getString("model"),
                            rs.getInt("year"),
                            rs.getString("category"),
                            rs.getString("transmission"),
                            rs.getDouble("daily_rate"),
                            rs.getString("status"),
                            rs.getString("imagePath")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cars.add(new Car(
                        rs.getInt("id"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("category"),
                        rs.getString("transmission"),
                        rs.getDouble("daily_rate"),
                        rs.getString("status"),
                        rs.getString("imagePath") // Added imagePath
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    public void updateCar(Car car) {
        String sql = "UPDATE cars SET make = ?, model = ?, year = ?, category = ?, transmission = ?, daily_rate = ?, status = ?, imagePath = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, car.getMake());
            pstmt.setString(2, car.getModel());
            pstmt.setInt(3, car.getYear());
            pstmt.setString(4, car.getCategory());
            pstmt.setString(5, car.getTransmission());
            pstmt.setDouble(6, car.getDailyRate());
            pstmt.setString(7, car.getStatus());
            pstmt.setString(8, car.getImagePath()); // Added imagePath
            pstmt.setInt(9, car.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Soft deletes a car by setting its status to 'Decommissioned'.
     * This preserves rental history and prevents data integrity issues.
     */
    public void deleteCar(int id) {
        String sql = "UPDATE cars SET status = 'Decommissioned' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("DIAGNOSTIC (CarDAO): Soft deleted car " + id + ". Affected rows: " + affectedRows);
        } catch (SQLException e) {
            System.err.println("ERROR (CarDAO): Failed to soft delete car: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Car> getAvailableCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars WHERE status = 'Available'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cars.add(new Car(
                        rs.getInt("id"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("category"),
                        rs.getString("transmission"),
                        rs.getDouble("daily_rate"),
                        rs.getString("status"),
                        rs.getString("imagePath") // Added imagePath
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }
}
