package com.example.carrentalmanagementsystem.dao;

import com.example.carrentalmanagementsystem.model.Customer;
import com.example.carrentalmanagementsystem.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public CustomerDAO() {
        // The createTable logic is now handled by DatabaseConnection.initializeDatabase()
    }

    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers(name, email, driversLicense, contact, status, username, password, birthDate) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"id"})) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getDriversLicense());
            pstmt.setString(4, customer.getContact());
            pstmt.setString(5, customer.getStatus());
            pstmt.setString(6, customer.getUsername());
            pstmt.setString(7, customer.getPassword()); // In a real system, this would be hashed
            pstmt.setString(8, customer.getBirthDate() != null ? customer.getBirthDate().toString() : null); // Add birthDate
            int affectedRows = pstmt.executeUpdate();
            System.out.println("DIAGNOSTIC (CustomerDAO): addCustomer() executed. Affected rows: " + affectedRows);
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        customer.setId(generatedKeys.getInt(1));
                        System.out.println("DIAGNOSTIC (CustomerDAO): New customer ID: " + customer.getId());
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR (CustomerDAO): Failed to add customer: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Customer getCustomerById(int customerId) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String birthDateStr = rs.getString("birthDate");
                LocalDate birthDate = (birthDateStr != null) ? LocalDate.parse(birthDateStr) : null;

                return new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("driversLicense"),
                        rs.getString("contact"),
                        0, // totalRentals is not stored directly, can be calculated if needed
                        null, // lastRentalDate is not stored directly
                        rs.getString("status"),
                        rs.getString("username"),
                        rs.getString("password"),
                        birthDate
                );
            }
        } catch (SQLException e) {
            System.err.println("ERROR (CustomerDAO): Failed to get customer by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.email, c.driversLicense, c.contact, c.status, c.username, c.password, c.birthDate, " +
                     "(SELECT COUNT(r.id) FROM rentals r WHERE r.customerId = c.id) as totalRentals, " +
                     "(SELECT MAX(r.rentalDate) FROM rentals r WHERE r.customerId = c.id) as lastRentalDate " +
                     "FROM customers c";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String lastRentalDateStr = rs.getString("lastRentalDate");
                LocalDate lastRental = (lastRentalDateStr != null) ? LocalDate.parse(lastRentalDateStr) : null;
                String birthDateStr = rs.getString("birthDate");
                LocalDate birthDate = (birthDateStr != null) ? LocalDate.parse(birthDateStr) : null;

                customers.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("driversLicense"),
                        rs.getString("contact"),
                        rs.getInt("totalRentals"),
                        lastRental,
                        rs.getString("status"),
                        rs.getString("username"),
                        rs.getString("password"),
                        birthDate // Pass birthDate
                ));
            }
            System.out.println("DIAGNOSTIC (CustomerDAO): getAllCustomers() fetched " + customers.size() + " customers.");
        } catch (SQLException e) {
            System.err.println("ERROR (CustomerDAO): Failed to get all customers: " + e.getMessage());
            e.printStackTrace();
        }
        return customers;
    }

    public void updateCustomerDetails(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ?, contact = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getContact());
            pstmt.setString(4, customer.getStatus());
            pstmt.setInt(5, customer.getId());
            int affectedRows = pstmt.executeUpdate();
            System.out.println("DIAGNOSTIC (CustomerDAO): updateCustomerDetails() executed. Affected rows: " + affectedRows);
        } catch (SQLException e) {
            System.err.println("ERROR (CustomerDAO): Failed to update customer details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateCustomerPassword(int customerId, String newPassword) {
        String sql = "UPDATE customers SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword); // Remember to hash this in a real application
            pstmt.setInt(2, customerId);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("DIAGNOSTIC (CustomerDAO): updateCustomerPassword() executed. Affected rows: " + affectedRows);
        } catch (SQLException e) {
            System.err.println("ERROR (CustomerDAO): Failed to update customer password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method updates all customer fields and should be used with caution.
     * Prefer updateCustomerDetails for most admin operations.
     */
    public void updateCustomerUnsafe(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ?, driversLicense = ?, contact = ?, status = ?, username = ?, password = ?, birthDate = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getDriversLicense());
            pstmt.setString(4, customer.getContact());
            pstmt.setString(5, customer.getStatus());
            pstmt.setString(6, customer.getUsername());
            pstmt.setString(7, customer.getPassword()); // In a real system, this would be hashed
            pstmt.setString(8, customer.getBirthDate() != null ? customer.getBirthDate().toString() : null); // Update birthDate
            pstmt.setInt(9, customer.getId());
            int affectedRows = pstmt.executeUpdate();
            System.out.println("DIAGNOSTIC (CustomerDAO): updateCustomerUnsafe() executed. Affected rows: " + affectedRows);
        } catch (SQLException e) {
            System.err.println("ERROR (CustomerDAO): Failed to update customer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteCustomer(int id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("DIAGNOSTIC (CustomerDAO): deleteCustomer() executed. Affected rows: " + affectedRows);
        } catch (SQLException e) {
            System.err.println("ERROR (CustomerDAO): Failed to delete customer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Customer getCustomerByIdentifier(String identifier) {
        String sql = "SELECT c.id, c.name, c.email, c.driversLicense, c.contact, c.status, c.username, c.password, c.birthDate, " +
                     "(SELECT COUNT(r.id) FROM rentals r WHERE r.customerId = c.id) as totalRentals, " +
                     "(SELECT MAX(r.rentalDate) FROM rentals r WHERE r.customerId = c.id) as lastRentalDate " +
                     "FROM customers c " +
                     "WHERE c.email = ? OR c.contact = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String lastRentalDateStr = rs.getString("lastRentalDate");
                LocalDate lastRental = (lastRentalDateStr != null) ? LocalDate.parse(lastRentalDateStr) : null;
                String birthDateStr = rs.getString("birthDate");
                LocalDate birthDate = (birthDateStr != null) ? LocalDate.parse(birthDateStr) : null;

                Customer customer = new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("driversLicense"),
                        rs.getString("contact"),
                        rs.getInt("totalRentals"),
                        lastRental,
                        rs.getString("status"),
                        rs.getString("username"),
                        rs.getString("password"),
                        birthDate
                );
                System.out.println("DIAGNOSTIC (CustomerDAO): getCustomerByIdentifier() found customer: " + customer.getName());
                return customer;
            }
            System.out.println("DIAGNOSTIC (CustomerDAO): getCustomerByIdentifier() found no customer for identifier: " + identifier);
        } catch (SQLException e) {
            System.err.println("ERROR (CustomerDAO): Failed to get customer by identifier: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Customer getCustomerByUsername(String username) {
        String sql = "SELECT c.id, c.name, c.email, c.driversLicense, c.contact, c.status, c.username, c.password, c.birthDate, " +
                     "(SELECT COUNT(r.id) FROM rentals r WHERE r.customerId = c.id) as totalRentals, " +
                     "(SELECT MAX(r.rentalDate) FROM rentals r WHERE r.customerId = c.id) as lastRentalDate " +
                     "FROM customers c " +
                     "WHERE c.username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String lastRentalDateStr = rs.getString("lastRentalDate");
                LocalDate lastRental = (lastRentalDateStr != null) ? LocalDate.parse(lastRentalDateStr) : null;
                String birthDateStr = rs.getString("birthDate");
                LocalDate birthDate = (birthDateStr != null) ? LocalDate.parse(birthDateStr) : null;

                Customer customer = new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("driversLicense"),
                        rs.getString("contact"),
                        rs.getInt("totalRentals"),
                        lastRental,
                        rs.getString("status"),
                        rs.getString("username"),
                        rs.getString("password"),
                        birthDate
                );
                System.out.println("DIAGNOSTIC (CustomerDAO): getCustomerByUsername() found customer: " + customer.getName());
                return customer;
            }
            System.out.println("DIAGNOSTIC (CustomerDAO): getCustomerByUsername() found no customer for username: " + username);
        } catch (SQLException e) {
            System.err.println("ERROR (CustomerDAO): Failed to get customer by username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
