package com.example.carrentalmanagementsystem.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Oracle Database URL
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/XE";
    private static final String USER = "NAOLAMU";
    private static final String PASSWORD = "naol123";

    private DatabaseConnection() {
        // private constructor to prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        System.out.println("DIAGNOSTIC: Attempting to get connection to Oracle database: " + URL);
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("DIAGNOSTIC: Successfully connected to database: " + conn.getMetaData().getURL());
        return conn;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            // --- DROP TABLES AND SEQUENCES TO ENSURE CLEAN SCHEMA ---
            // WARNING: THIS WILL DELETE ALL DATA ON STARTUP
            // Remove these lines in production!
            // dropTable(stmt, "maintenance");
            // dropTable(stmt, "rentals");
            // dropTable(stmt, "cars");
            // dropTable(stmt, "customers");
            // dropTable(stmt, "users");
            
            // dropSequence(stmt, "maintenance_seq");
            // dropSequence(stmt, "rentals_seq");
            // dropSequence(stmt, "cars_seq");
            // dropSequence(stmt, "customers_seq");
            // dropSequence(stmt, "users_seq");


            // --- Create Users Table ---
            createTableWithSequence(stmt, "users", "users_seq", "CREATE TABLE users (" +
                    "id NUMBER PRIMARY KEY, " +
                    "username VARCHAR2(255) NOT NULL UNIQUE, " +
                    "password VARCHAR2(255) NOT NULL, " +
                    "role VARCHAR2(50) NOT NULL)");

            // --- Create Cars Table ---
            createTableWithSequence(stmt, "cars", "cars_seq", "CREATE TABLE cars (" +
                    "id NUMBER PRIMARY KEY, " +
                    "make VARCHAR2(255), " +
                    "model VARCHAR2(255) NOT NULL, " +
                    "year NUMBER NOT NULL, " +
                    "category VARCHAR2(100), " +
                    "transmission VARCHAR2(50), " +
                    "daily_rate NUMBER, " +
                    "status VARCHAR2(50) DEFAULT 'Available' NOT NULL, " +
                    "imagePath VARCHAR2(500))");

            // --- Create Customers Table ---
            createTableWithSequence(stmt, "customers", "customers_seq", "CREATE TABLE customers (" +
                    "id NUMBER PRIMARY KEY, " +
                    "name VARCHAR2(255) NOT NULL, " +
                    "email VARCHAR2(255) NOT NULL UNIQUE, " +
                    "contact VARCHAR2(50) NOT NULL, " +
                    "driversLicense VARCHAR2(100) NOT NULL UNIQUE, " +
                    "username VARCHAR2(255) UNIQUE, " +
                    "password VARCHAR2(255), " +
                    "status VARCHAR2(50) DEFAULT 'New' NOT NULL," +
                    "birthDate VARCHAR2(50))");

            // --- Create Rentals Table ---
            createTableWithSequence(stmt, "rentals", "rentals_seq", "CREATE TABLE rentals (" +
                    "id NUMBER PRIMARY KEY, " +
                    "carId NUMBER NOT NULL, " +
                    "customerId NUMBER NOT NULL, " +
                    "rentalDate VARCHAR2(50) NOT NULL, " +
                    "returnDate VARCHAR2(50), " +
                    "finalPrice NUMBER, " +
                    "status VARCHAR2(50) DEFAULT 'Pending' NOT NULL, " + // Added status field
                    "FOREIGN KEY (carId) REFERENCES cars(id), " +
                    "FOREIGN KEY (customerId) REFERENCES customers(id))");

            // --- Create Maintenance Table ---
            // Updated to match MaintenanceDAO fields: start_date, end_date
            createTableWithSequence(stmt, "maintenance", "maintenance_seq", "CREATE TABLE maintenance (" +
                    "id NUMBER PRIMARY KEY, " +
                    "car_id NUMBER NOT NULL, " +
                    "start_date VARCHAR2(50) NOT NULL, " +
                    "end_date VARCHAR2(50), " +
                    "description VARCHAR2(500), " +
                    "cost NUMBER, " +
                    "FOREIGN KEY (car_id) REFERENCES cars(id))");

            // --- Diagnostic Check: Add a default admin user if the table is empty ---
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM users")) {
                if (rs.next()) {
                    int userCount = rs.getInt("count");
                    System.out.println("DIAGNOSTIC: Found " + userCount + " users in the database.");
                    if (userCount == 0) {
                        // For sequence-based tables, we don't insert the ID, the trigger handles it.
                        stmt.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'adminpass', 'admin')");
                        System.out.println("Default admin user created.");
                    }
                }
            }
            
            System.out.println("DIAGNOSTIC: Database initialization process completed.");

        } catch (SQLException e) {
            System.err.println("ERROR: Exception during database initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void dropTable(Statement stmt, String tableName) {
        try {
            stmt.execute("DROP TABLE " + tableName + " CASCADE CONSTRAINTS");
            System.out.println("DIAGNOSTIC: Dropped table " + tableName);
        } catch (SQLException e) {
            // Ignore if table doesn't exist (ORA-00942)
            if (e.getErrorCode() != 942) {
                System.err.println("WARNING: Could not drop table " + tableName + ": " + e.getMessage());
            }
        }
    }

    private static void dropSequence(Statement stmt, String sequenceName) {
        try {
            stmt.execute("DROP SEQUENCE " + sequenceName);
            System.out.println("DIAGNOSTIC: Dropped sequence " + sequenceName);
        } catch (SQLException e) {
            // Ignore if sequence doesn't exist (ORA-02289)
            if (e.getErrorCode() != 2289) {
                System.err.println("WARNING: Could not drop sequence " + sequenceName + ": " + e.getMessage());
            }
        }
    }

    private static void createTableWithSequence(Statement stmt, String tableName, String sequenceName, String createTableSql) {
        try {
            // 1. Create Table
            try {
                stmt.execute(createTableSql);
                System.out.println("DIAGNOSTIC: " + tableName + " table created.");
            } catch (SQLException e) {
                if (e.getErrorCode() == 955) { // ORA-00955: name is already used
                    System.out.println("DIAGNOSTIC: " + tableName + " table already exists.");
                    return;
                } else {
                    throw e;
                }
            }

            // 2. Create Sequence
            try {
                stmt.execute("CREATE SEQUENCE " + sequenceName + " START WITH 1 INCREMENT BY 1");
                System.out.println("DIAGNOSTIC: Sequence " + sequenceName + " created.");
            } catch (SQLException e) {
                if (e.getErrorCode() == 955) {
                    System.out.println("DIAGNOSTIC: Sequence " + sequenceName + " already exists.");
                } else {
                    throw e;
                }
            }

            // 3. Create Trigger
            String triggerSql = "CREATE OR REPLACE TRIGGER " + tableName + "_trg " +
                    "BEFORE INSERT ON " + tableName + " " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "  SELECT " + sequenceName + ".NEXTVAL INTO :new.id FROM dual; " +
                    "END;";
            stmt.execute(triggerSql);
            System.out.println("DIAGNOSTIC: Trigger for " + tableName + " created.");

        } catch (SQLException e) {
            System.err.println("ERROR: Could not setup table " + tableName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
