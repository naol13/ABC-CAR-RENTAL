package com.example.carrentalmanagementsystem.dao;

import com.example.carrentalmanagementsystem.model.Rental;
import com.example.carrentalmanagementsystem.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentalDAO {

    public RentalDAO() {
        // The createTable logic is now handled by DatabaseConnection.initializeDatabase()
    }

    public void addRental(Rental rental) {
        String sql = "INSERT INTO rentals(carId, customerId, rentalDate, returnDate, finalPrice, status) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rental.getCarId());
            pstmt.setInt(2, rental.getCustomerId());
            pstmt.setString(3, rental.getRentalDate().toString());
            pstmt.setString(4, rental.getReturnDate() != null ? rental.getReturnDate().toString() : null);
            pstmt.setDouble(5, rental.getFinalPrice());
            pstmt.setString(6, rental.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding rental to the database", e);
        }
    }

    public List<Rental> getAllRentals() {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rentals.add(new Rental(
                        rs.getInt("id"),
                        rs.getInt("carId"),
                        rs.getInt("customerId"),
                        LocalDate.parse(rs.getString("rentalDate")),
                        rs.getString("returnDate") != null ? LocalDate.parse(rs.getString("returnDate")) : null,
                        rs.getDouble("finalPrice"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rentals;
    }

    public List<Rental> getRentalsByCustomerId(int customerId) {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals WHERE customerId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rentals.add(new Rental(
                            rs.getInt("id"),
                            rs.getInt("carId"),
                            rs.getInt("customerId"),
                            LocalDate.parse(rs.getString("rentalDate")),
                            rs.getString("returnDate") != null ? LocalDate.parse(rs.getString("returnDate")) : null,
                            rs.getDouble("finalPrice"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rentals;
    }

    public void updateRental(Rental rental) {
        String sql = "UPDATE rentals SET carId = ?, customerId = ?, rentalDate = ?, returnDate = ?, finalPrice = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rental.getCarId());
            pstmt.setInt(2, rental.getCustomerId());
            pstmt.setString(3, rental.getRentalDate().toString());
            pstmt.setString(4, rental.getReturnDate() != null ? rental.getReturnDate().toString() : null);
            pstmt.setDouble(5, rental.getFinalPrice());
            pstmt.setString(6, rental.getStatus());
            pstmt.setInt(7, rental.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Rental> getOverdueRentals() {
        List<Rental> overdueRentals = new ArrayList<>();
        // Fetch all active rentals and filter in Java for simplicity with date parsing
        String sql = "SELECT * FROM rentals WHERE status = 'Active'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String returnDateStr = rs.getString("returnDate");
                if (returnDateStr != null) {
                    LocalDate returnDate = LocalDate.parse(returnDateStr);
                    if (returnDate.isBefore(LocalDate.now())) {
                        overdueRentals.add(new Rental(
                                rs.getInt("id"),
                                rs.getInt("carId"),
                                rs.getInt("customerId"),
                                LocalDate.parse(rs.getString("rentalDate")),
                                returnDate,
                                rs.getDouble("finalPrice"),
                                rs.getString("status")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return overdueRentals;
    }
}
