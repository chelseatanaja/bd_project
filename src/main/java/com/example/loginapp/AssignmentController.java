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

public class AssignmentController {

    @FXML private TextField txtAssignmentId;
    @FXML private TextField txtAssignmentNama;
    @FXML private DatePicker dpTanggalUnggah;
    @FXML private DatePicker dpDeadline;
    @FXML private TextField txtAssignmentFile;
    @FXML private TextField txtAssignmentJamAkses;
    @FXML private TextArea txtAssignmentDeskripsi;
    @FXML private TextField txtSearchAssignment;

    @FXML private Button btnAddAssignment;
    @FXML private Button btnEditAssignment;
    @FXML private Button btnDeleteAssignment;
    @FXML private Button btnClearAssignment;
    @FXML private Button btnBack;

    @FXML private TableView<Assignment> tblAssignment;
    @FXML private TableColumn<Assignment, String> colAssignmentId;
    @FXML private TableColumn<Assignment, String> colAssignmentNama;
    @FXML private TableColumn<Assignment, String> colTanggalUnggah;
    @FXML private TableColumn<Assignment, String> colDeadline;
    @FXML private TableColumn<Assignment, String> colAssignmentFile;

    private ObservableList<Assignment> assignmentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colAssignmentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAssignmentNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colTanggalUnggah.setCellValueFactory(new PropertyValueFactory<>("tanggalUpload"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        colAssignmentFile.setCellValueFactory(new PropertyValueFactory<>("file"));

        loadAssignment();

        btnAddAssignment.setOnAction(e -> addAssignment());
        btnEditAssignment.setOnAction(e -> editAssignment());
        btnDeleteAssignment.setOnAction(e -> deleteAssignment());
        btnClearAssignment.setOnAction(e -> clearForm());
        btnBack.setOnAction(e -> moveTo("admin-dashboard.fxml"));

        tblAssignment.setOnMouseClicked(e -> selectAssignment());
        txtSearchAssignment.textProperty().addListener((obs, oldVal, newVal) -> searchAssignment(newVal));
        btnBack.setOnAction(
                e -> moveTo("admin-dashboard.fxml")
        );
    }

    private void loadAssignment() {
        assignmentList.clear();

        String sql = "SELECT * FROM assignment ORDER BY id";

        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                assignmentList.add(new Assignment(
                        rs.getString("id"),
                        rs.getString("nama"),
                        rs.getString("tanggal_upload"),
                        rs.getString("deadline"),
                        rs.getString("file")
                ));
            }

            tblAssignment.setItems(assignmentList);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Gagal mengambil data assignment.");
        }
    }

    private void addAssignment() {
        String sql = "INSERT INTO assignment(id, nama, tanggal_upload, deadline, file) VALUES (?, ?, ?, ?, ?)";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtAssignmentId.getText());
            ps.setString(2, txtAssignmentNama.getText());
            ps.setDate(3, Date.valueOf(dpTanggalUnggah.getValue()));
            ps.setDate(4, Date.valueOf(dpDeadline.getValue()));
            ps.setString(5, txtAssignmentFile.getText());

            ps.executeUpdate();

            showAlert("Success", "Data assignment berhasil ditambahkan.");
            loadAssignment();
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menambahkan data assignment.");
        }
    }

    private void editAssignment() {
        String sql = "UPDATE assignment SET nama=?, tanggal_upload=?, deadline=?, file=? WHERE id=?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtAssignmentNama.getText());
            ps.setDate(2, Date.valueOf(dpTanggalUnggah.getValue()));
            ps.setDate(3, Date.valueOf(dpDeadline.getValue()));
            ps.setString(4, txtAssignmentFile.getText());
            ps.setString(5, txtAssignmentId.getText());

            ps.executeUpdate();

            showAlert("Success", "Data assignment berhasil diupdate.");
            loadAssignment();
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal mengupdate data assignment.");
        }
    }

    private void deleteAssignment() {
        String sql = "DELETE FROM assignment WHERE id=?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtAssignmentId.getText());
            ps.executeUpdate();

            showAlert("Success", "Data assignment berhasil dihapus.");
            loadAssignment();
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menghapus data assignment.");
        }
    }

    private void searchAssignment(String keyword) {
        assignmentList.clear();

        String sql = "SELECT * FROM assignment WHERE LOWER(nama) LIKE LOWER(?) OR LOWER(id) LIKE LOWER(?) ORDER BY id";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                assignmentList.add(new Assignment(
                        rs.getString("id"),
                        rs.getString("nama"),
                        rs.getString("tanggal_upload"),
                        rs.getString("deadline"),
                        rs.getString("file")
                ));
            }

            tblAssignment.setItems(assignmentList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectAssignment() {
        Assignment selected = tblAssignment.getSelectionModel().getSelectedItem();

        if (selected != null) {
            txtAssignmentId.setText(selected.getId());
            txtAssignmentNama.setText(selected.getNama());
            txtAssignmentFile.setText(selected.getFile());
        }
    }

    private void clearForm() {
        txtAssignmentId.clear();
        txtAssignmentNama.clear();
        dpTanggalUnggah.setValue(null);
        dpDeadline.setValue(null);
        txtAssignmentFile.clear();
        txtAssignmentJamAkses.clear();
        txtAssignmentDeskripsi.clear();
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

    public static class Assignment {
        private String id;
        private String nama;
        private String tanggalUpload;
        private String deadline;
        private String file;

        public Assignment(String id, String nama, String tanggalUpload, String deadline, String file) {
            this.id = id;
            this.nama = nama;
            this.tanggalUpload = tanggalUpload;
            this.deadline = deadline;
            this.file = file;
        }

        public String getId() { return id; }
        public String getNama() { return nama; }
        public String getTanggalUpload() { return tanggalUpload; }
        public String getDeadline() { return deadline; }
        public String getFile() { return file; }
    }
}