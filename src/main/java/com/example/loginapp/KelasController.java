package com.example.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;

public class KelasController {

    @FXML private TextField txtKelasId;
    @FXML private TextField txtKelasNama;
    @FXML private TextField txtKelasKapasitas;
    @FXML private TextField txtKelasSemester;
    @FXML private TextField txtSearchKelas;

    @FXML private Button btnAddKelas;
    @FXML private Button btnEditKelas;
    @FXML private Button btnDeleteKelas;
    @FXML private Button btnClearKelas;
    @FXML private Button btnBack;

    @FXML private TableView<Kelas> tblKelas;
    @FXML private TableColumn<Kelas, String> colKelasId;
    @FXML private TableColumn<Kelas, String> colKelasNama;
    @FXML private TableColumn<Kelas, Integer> colKelasKapasitas;
    @FXML private TableColumn<Kelas, Integer> colKelasSemester;

    private ObservableList<Kelas> kelasList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colKelasId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colKelasNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colKelasKapasitas.setCellValueFactory(new PropertyValueFactory<>("kapasitas"));
        colKelasSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));

        loadKelas();

        btnAddKelas.setOnAction(e -> addKelas());
        btnEditKelas.setOnAction(e -> editKelas());
        btnDeleteKelas.setOnAction(e -> deleteKelas());
        btnClearKelas.setOnAction(e -> clearForm());
        btnBack.setOnAction(e -> moveTo("admin_dashboard.fxml"));

        tblKelas.setOnMouseClicked(e -> selectKelas());
        txtSearchKelas.textProperty().addListener((obs, oldVal, newVal) -> searchKelas(newVal));
        btnBack.setOnAction(

                e -> moveTo("admin_dashboard.fxml")
        );
    }

    private void loadKelas() {
        kelasList.clear();

        String sql = "SELECT * FROM kelas ORDER BY id";

        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                kelasList.add(new Kelas(
                        rs.getString("id"),
                        rs.getString("nama"),
                        rs.getInt("kapasitas"),
                        rs.getInt("semester")
                ));
            }

            tblKelas.setItems(kelasList);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Gagal mengambil data kelas.");
        }
    }

    private void addKelas() {
        String sql = "INSERT INTO kelas(id, nama, kapasitas, semester) VALUES (?, ?, ?, ?)";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtKelasId.getText());
            ps.setString(2, txtKelasNama.getText());
            ps.setInt(3, Integer.parseInt(txtKelasKapasitas.getText()));
            ps.setInt(4, Integer.parseInt(txtKelasSemester.getText()));

            ps.executeUpdate();

            showAlert("Success", "Data kelas berhasil ditambahkan.");
            loadKelas();
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menambahkan data kelas.");
        }
    }

    private void editKelas() {
        String sql = "UPDATE kelas SET nama=?, kapasitas=?, semester=? WHERE id=?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtKelasNama.getText());
            ps.setInt(2, Integer.parseInt(txtKelasKapasitas.getText()));
            ps.setInt(3, Integer.parseInt(txtKelasSemester.getText()));
            ps.setString(4, txtKelasId.getText());

            ps.executeUpdate();

            showAlert("Success", "Data kelas berhasil diupdate.");
            loadKelas();
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal mengupdate data kelas.");
        }
    }

    private void deleteKelas() {
        String sql = "DELETE FROM kelas WHERE id=?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtKelasId.getText());
            ps.executeUpdate();

            showAlert("Success", "Data kelas berhasil dihapus.");
            loadKelas();
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menghapus data kelas.");
        }
    }

    private void searchKelas(String keyword) {
        kelasList.clear();

        String sql = "SELECT * FROM kelas WHERE LOWER(nama) LIKE LOWER(?) OR LOWER(id) LIKE LOWER(?) ORDER BY id";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                kelasList.add(new Kelas(
                        rs.getString("id"),
                        rs.getString("nama"),
                        rs.getInt("kapasitas"),
                        rs.getInt("semester")
                ));
            }

            tblKelas.setItems(kelasList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectKelas() {
        Kelas selected = tblKelas.getSelectionModel().getSelectedItem();

        if (selected != null) {
            txtKelasId.setText(selected.getId());
            txtKelasNama.setText(selected.getNama());
            txtKelasKapasitas.setText(String.valueOf(selected.getKapasitas()));
            txtKelasSemester.setText(String.valueOf(selected.getSemester()));
        }
    }

    private void clearForm() {
        txtKelasId.clear();
        txtKelasNama.clear();
        txtKelasKapasitas.clear();
        txtKelasSemester.clear();
    }

    private void moveTo(String fxml) {
        try {

            Stage stage = (Stage) btnBack.getScene().getWindow();

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Kelas {
        private String id;
        private String nama;
        private int kapasitas;
        private int semester;

        public Kelas(String id, String nama, int kapasitas, int semester) {
            this.id = id;
            this.nama = nama;
            this.kapasitas = kapasitas;
            this.semester = semester;
        }

        public String getId() { return id; }
        public String getNama() { return nama; }
        public int getKapasitas() { return kapasitas; }
        public int getSemester() { return semester; }
    }
}