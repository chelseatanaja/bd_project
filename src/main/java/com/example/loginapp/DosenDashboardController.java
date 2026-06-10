package com.example.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class DosenDashboardController {

    @FXML private Label lblTotalKelas;
    @FXML private Label lblTotalAssignment;

    @FXML private Button btnLogout;

    @FXML private TableView<DosenAssignment> tblDosenAssignment;
    @FXML private TableColumn<DosenAssignment, String> colAssignmentId;
    @FXML private TableColumn<DosenAssignment, String> colAssignmentNama;
    @FXML private TableColumn<DosenAssignment, String> colKelas;
    @FXML private TableColumn<DosenAssignment, String> colDeadline;

    private final ObservableList<DosenAssignment> assignmentList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colAssignmentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAssignmentNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colKelas.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));

        assignmentList.addAll(
                new DosenAssignment("A001", "ERD Project", "Basis Data A", "2026-06-15"),
                new DosenAssignment("A002", "SQL Query Task", "Basis Data B", "2026-06-18"),
                new DosenAssignment("A003", "JavaFX CRUD App", "PBO A", "2026-06-20")
        );

        tblDosenAssignment.setItems(assignmentList);

        lblTotalKelas.setText("5");
        lblTotalAssignment.setText(String.valueOf(assignmentList.size()));

        btnLogout.setOnAction(e -> moveTo("login.fxml"));
    }

    private void moveTo(String fxml) {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/loginapp/" + fxml)
            );

            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class DosenAssignment {
        private final String id;
        private final String nama;
        private final String kelas;
        private final String deadline;

        public DosenAssignment(String id, String nama, String kelas, String deadline) {
            this.id = id;
            this.nama = nama;
            this.kelas = kelas;
            this.deadline = deadline;
        }

        public String getId() {
            return id;
        }

        public String getNama() {
            return nama;
        }

        public String getKelas() {
            return kelas;
        }

        public String getDeadline() {
            return deadline;
        }
    }
}