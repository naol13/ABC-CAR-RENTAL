package com.example.carrentalmanagementsystem.controller;

import com.example.carrentalmanagementsystem.dao.MaintenanceDAO;
import com.example.carrentalmanagementsystem.model.Maintenance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class MaintenanceManagementController {

    @FXML
    private TableView<Maintenance> maintenanceTableView;
    @FXML
    private TableColumn<Maintenance, Integer> idColumn;
    @FXML
    private TableColumn<Maintenance, Integer> carIdColumn;
    @FXML
    private TableColumn<Maintenance, LocalDate> startDateColumn;
    @FXML
    private TableColumn<Maintenance, LocalDate> endDateColumn;
    @FXML
    private TableColumn<Maintenance, String> descriptionColumn;
    @FXML
    private TableColumn<Maintenance, Double> costColumn;

    private MaintenanceDAO maintenanceDAO;
    private ObservableList<Maintenance> maintenanceList;
    private MainController mainController;

    @FXML
    public void initialize() {
        maintenanceDAO = new MaintenanceDAO();
        refreshTable();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void refreshTable() {
        maintenanceList = FXCollections.observableArrayList(maintenanceDAO.getAllMaintenance());

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        carIdColumn.setCellValueFactory(new PropertyValueFactory<>("carId"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));

        maintenanceTableView.setItems(maintenanceList);
    }

    @FXML
    private void handleAddNewMaintenance() {
        if (mainController != null) {
            mainController.openMaintenanceForm(null);
        }
    }
}
