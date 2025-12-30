package com.example.carrentalmanagementsystem.dao;

import com.example.carrentalmanagementsystem.model.Maintenance;
import com.example.carrentalmanagementsystem.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAO {

    public MaintenanceDAO() {
        // The createTable logic is now handled by DatabaseConnection.initializeDatabase()
    }

    public void addMaintenance(Maintenance maintenance) {
        String sql = "INSERT INTO maintenance(car_id, start_date, end_date, description, cost) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"id"})) {
            pstmt.setInt(1, maintenance.getCarId());
            pstmt.setString(2, maintenance.getStartDate().toString());
            pstmt.setString(3, maintenance.getEndDate() != null ? maintenance.getEndDate().toString() : null);
            pstmt.setString(4, maintenance.getDescription());
            pstmt.setDouble(5, maintenance.getCost());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        maintenance.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Maintenance> getAllMaintenance() {
        List<Maintenance> maintenanceList = new ArrayList<>();
        String sql = "SELECT * FROM maintenance";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                maintenanceList.add(new Maintenance(
                        rs.getInt("id"),
                        rs.getInt("car_id"),
                        LocalDate.parse(rs.getString("start_date")),
                        rs.getString("end_date") != null ? LocalDate.parse(rs.getString("end_date")) : null,
                        rs.getString("description"),
                        rs.getDouble("cost")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maintenanceList;
    }

    public void updateMaintenance(Maintenance maintenance) {
        String sql = "UPDATE maintenance SET car_id = ?, start_date = ?, end_date = ?, description = ?, cost = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maintenance.getCarId());
            pstmt.setString(2, maintenance.getStartDate().toString());
            pstmt.setString(3, maintenance.getEndDate() != null ? maintenance.getEndDate().toString() : null);
            pstmt.setString(4, maintenance.getDescription());
            pstmt.setDouble(5, maintenance.getCost());
            pstmt.setInt(6, maintenance.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
