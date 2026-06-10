package com.example.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminDashboardController {

    @FXML private Button btnDashboard;
    @FXML private Button btnKelas;
    @FXML private Button btnAssignment;
    @FXML private Button btnLogout;
    @FXML private Button btnRefresh;

    @FXML private Label lblTotalKelas;
    @FXML private Label lblTotalAssignment;
    @FXML private Label lblTotalUser;

    @FXML private TableView<RecentData> tblRecent;
    @FXML private TableColumn<RecentData, String> colRecentId;
    @FXML private TableColumn<RecentData, String> colRecentType;
    @FXML private TableColumn<RecentData, String> colRecentName;
    @FXML private TableColumn<RecentData, String> colRecentStatus;

    private final ObservableList<RecentData> recentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colRecentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRecentType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRecentName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRecentStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadDashboardData();

        btnKelas.setOnAction(e -> moveTo("kelas.fxml"));
        btnAssignment.setOnAction(e -> moveTo("assignment.fxml"));
        btnLogout.setOnAction(e -> moveTo("login.fxml"));
        btnRefresh.setOnAction(e -> loadDashboardData());
    }

    private void loadDashboardData() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();

            ResultSet rsKelas = st.executeQuery("SELECT COUNT(*) FROM kelas");
            if (rsKelas.next()) {
                lblTotalKelas.setText(String.valueOf(rsKelas.getInt(1)));
            }

            ResultSet rsAssignment = st.executeQuery("SELECT COUNT(*) FROM assignment");
            if (rsAssignment.next()) {
                lblTotalAssignment.setText(String.valueOf(rsAssignment.getInt(1)));
            }

            ResultSet rsUser = st.executeQuery("SELECT COUNT(*) FROM users");
            if (rsUser.next()) {
                lblTotalUser.setText(String.valueOf(rsUser.getInt(1)));
            }

            recentList.clear();

            ResultSet rsRecentKelas = st.executeQuery("SELECT id, nama FROM kelas ORDER BY id LIMIT 3");
            while (rsRecentKelas.next()) {
                recentList.add(new RecentData(
                        rsRecentKelas.getString("id"),
                        "Kelas",
                        rsRecentKelas.getString("nama"),
                        "Active"
                ));
            }

            ResultSet rsRecentAssignment = st.executeQuery("SELECT id, nama FROM assignment ORDER BY id LIMIT 3");
            while (rsRecentAssignment.next()) {
                recentList.add(new RecentData(
                        rsRecentAssignment.getString("id"),
                        "Assignment",
                        rsRecentAssignment.getString("nama"),
                        "Open"
                ));
            }

            tblRecent.setItems(recentList);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Gagal mengambil data dashboard.");
        }
    }

    private void moveTo(String fxml) {
        try {
            String path = "/com/example/loginapp/" + fxml;
            java.net.URL url = getClass().getResource(path);

            System.out.println("Mencari FXML: " + path);
            System.out.println("Hasil URL: " + url);

            if (url == null) {
                showAlert("FXML Error", "File tidak ditemukan:\n" + path);
                return;
            }

            Stage stage = (Stage) btnLogout.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(url);

            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class RecentData {
        private final String id;
        private final String type;
        private final String name;
        private final String status;

        public RecentData(String id, String type, String name, String status) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.status = status;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getName() { return name; }
        public String getStatus() { return status; }
    }
}