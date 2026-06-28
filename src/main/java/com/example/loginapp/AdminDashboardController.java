package com.example.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Optional;

public class AdminDashboardController {

    // === SIDEBAR ===
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavKelas;
    @FXML private Button btnNavStudents;
    @FXML private Label lblSidebarNama;
    @FXML private Label lblSidebarEmail;

    // === VIEWS ===
    @FXML private VBox viewDashboard;
    @FXML private VBox viewKelas;
    @FXML private VBox viewStudents;

    // === DASHBOARD ===
    @FXML private Label lblTotalKelas;
    @FXML private Label lblTotalUser;
    @FXML private Label lblTotalAssignment;
    @FXML private TableView<RecentData> tblRecent;
    @FXML private TableColumn<RecentData, String> colRecentId;
    @FXML private TableColumn<RecentData, String> colRecentType;
    @FXML private TableColumn<RecentData, String> colRecentName;
    @FXML private TableColumn<RecentData, String> colRecentStatus;
    private final ObservableList<RecentData> recentList = FXCollections.observableArrayList();

    // === KELAS ===
    @FXML private TextField txtKelasId;
    @FXML private TextField txtKelasNama;
    @FXML private TextField txtKelasKapasitas;
    @FXML private TextField txtKelasSemester;
    @FXML private TextField txtSearchKelas;
    @FXML private TableView<Kelas> tblKelas;
    @FXML private TableColumn<Kelas, String> colKelasId;
    @FXML private TableColumn<Kelas, String> colKelasNama;
    @FXML private TableColumn<Kelas, Integer> colKelasKapasitas;
    @FXML private TableColumn<Kelas, Integer> colKelasSemester;
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();

    // === STUDENTS ===
    @FXML private TextField txtStudentId;
    @FXML private TextField txtStudentNama;
    @FXML private TextField txtStudentEmail;
    @FXML private TextField txtSearchStudent;
    @FXML private TableView<Student> tblStudents;
    @FXML private TableColumn<Student, Integer> colStudentId;
    @FXML private TableColumn<Student, String> colStudentNama;
    @FXML private TableColumn<Student, String> colStudentEmail;
    @FXML private TableColumn<Student, String> colStudentRole;
    private final ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Sidebar user info
        lblSidebarNama.setText(Session.getNama() != null ? Session.getNama() : "Admin");
        lblSidebarEmail.setText(Session.getEmail() != null ? Session.getEmail() : "");

        // Setup tables
        colRecentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRecentType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRecentName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRecentStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colKelasId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colKelasNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colKelasKapasitas.setCellValueFactory(new PropertyValueFactory<>("kapasitas"));
        colKelasSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        tblKelas.setItems(kelasList);
        tblKelas.setOnMouseClicked(e -> selectKelas());
        txtSearchKelas.textProperty().addListener((obs, o, n) -> searchKelas(n));

        colStudentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colStudentEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStudentRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        tblStudents.setItems(studentList);
        tblStudents.setOnMouseClicked(e -> selectStudent());
        txtSearchStudent.textProperty().addListener((obs, o, n) -> searchStudents(n));

        showView("dashboard");
        loadDashboard();
    }

    // ===================== NAV =====================

    @FXML private void handleNavDashboard() {
        showView("dashboard");
        loadDashboard();
        setActiveNav(btnNavDashboard);
    }

    @FXML private void handleNavKelas() {
        showView("kelas");
        loadKelas();
        setActiveNav(btnNavKelas);
    }

    @FXML private void handleNavStudents() {
        showView("students");
        loadStudents();
        setActiveNav(btnNavStudents);
    }

    @FXML private void handleLogout() {
        Session.clearSession();
        moveTo("login.fxml");
    }

    private void showView(String view) {
        viewDashboard.setVisible(false);
        viewKelas.setVisible(false);
        viewStudents.setVisible(false);
        switch (view) {
            case "dashboard" -> viewDashboard.setVisible(true);
            case "kelas" -> viewKelas.setVisible(true);
            case "students" -> viewStudents.setVisible(true);
        }
    }

    private void setActiveNav(Button active) {
        String inactive = "-fx-background-color:transparent; -fx-text-fill:#CBD5E1; -fx-font-size:14; -fx-alignment:CENTER_LEFT; -fx-cursor:hand; -fx-background-radius:12;";
        String activeStyle = "-fx-background-color:linear-gradient(to right,#4F46E5,#2563EB); -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:12; -fx-alignment:CENTER_LEFT; -fx-cursor:hand;";
        btnNavDashboard.setStyle(inactive);
        btnNavKelas.setStyle(inactive);
        btnNavStudents.setStyle(inactive);
        active.setStyle(activeStyle);
    }

    // ===================== DASHBOARD =====================

    private void loadDashboard() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {

            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM kelas");
            if (rs.next()) lblTotalKelas.setText(String.valueOf(rs.getInt(1)));

            rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE role='Student'");
            if (rs.next()) lblTotalUser.setText(String.valueOf(rs.getInt(1)));

            rs = st.executeQuery("SELECT COUNT(*) FROM assignment");
            if (rs.next()) lblTotalAssignment.setText(String.valueOf(rs.getInt(1)));

            recentList.clear();
            rs = st.executeQuery("SELECT id, nama FROM kelas ORDER BY id DESC LIMIT 5");
            while (rs.next())
                recentList.add(new RecentData(rs.getString("id"), "Kelas", rs.getString("nama"), "Active"));
            tblRecent.setItems(recentList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== KELAS =====================

    private void loadKelas() {
        kelasList.clear();
        String query = "SELECT id, nama, kapasitas, semester FROM kelas"; // Sebutkan kolomnya secara eksplisit lebih aman

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                kelasList.add(new Kelas(
                        rs.getString("id"),
                        rs.getString("nama"), // 	Keadaan kolom di DB adalah "nama"
                        rs.getInt("kapasitas"),
                        rs.getInt("semester")
                ));
            }
            tblKelas.setItems(kelasList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleAddKelas() {
        if (txtKelasId.getText().isEmpty() || txtKelasNama.getText().isEmpty()) {
            showAlert("Gagal", "ID dan Nama Kelas wajib diisi.");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO kelas(id, nama, kapasitas, semester) VALUES (?,?,?,?)")) {
            ps.setString(1, txtKelasId.getText().trim());
            ps.setString(2, txtKelasNama.getText().trim());
            ps.setInt(3, txtKelasKapasitas.getText().isEmpty() ? 0 : Integer.parseInt(txtKelasKapasitas.getText()));
            ps.setInt(4, txtKelasSemester.getText().isEmpty() ? 0 : Integer.parseInt(txtKelasSemester.getText()));
            ps.executeUpdate();
            showInfo("Berhasil", "Kelas ditambahkan.");
            loadKelas();
            handleClearKelas();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
    }

    @FXML private void handleEditKelas() {
        if (txtKelasId.getText().isEmpty()) { showAlert("Pilih Data", "Pilih kelas dulu."); return; }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE kelas SET nama=?, kapasitas=?, semester=? WHERE id=?")) {
            ps.setString(1, txtKelasNama.getText().trim());
            ps.setInt(2, txtKelasKapasitas.getText().isEmpty() ? 0 : Integer.parseInt(txtKelasKapasitas.getText()));
            ps.setInt(3, txtKelasSemester.getText().isEmpty() ? 0 : Integer.parseInt(txtKelasSemester.getText()));
            ps.setString(4, txtKelasId.getText().trim());
            ps.executeUpdate();
            showInfo("Berhasil", "Kelas diupdate.");
            loadKelas();
            handleClearKelas();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
    }

    @FXML private void handleDeleteKelas() {
        if (txtKelasId.getText().isEmpty()) { showAlert("Pilih Data", "Pilih kelas dulu."); return; }
        Optional<ButtonType> result = confirm("Yakin hapus kelas ini?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM kelas WHERE id=?")) {
                ps.setString(1, txtKelasId.getText());
                ps.executeUpdate();
                showInfo("Berhasil", "Kelas dihapus.");
                loadKelas();
                handleClearKelas();
            } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
        }
    }

    @FXML private void handleClearKelas() {
        txtKelasId.clear(); txtKelasNama.clear();
        txtKelasKapasitas.clear(); txtKelasSemester.clear();
        tblKelas.getSelectionModel().clearSelection();
    }

    private void selectKelas() {
        Kelas s = tblKelas.getSelectionModel().getSelectedItem();
        if (s != null) {
            txtKelasId.setText(s.getId());
            txtKelasNama.setText(s.getNama());
            txtKelasKapasitas.setText(String.valueOf(s.getKapasitas()));
            txtKelasSemester.setText(String.valueOf(s.getSemester()));
        }
    }

    private void searchKelas(String kw) {
        kelasList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM kelas WHERE LOWER(nama) LIKE ? OR LOWER(id::text) LIKE ? ORDER BY id")) {
            ps.setString(1, "%" + kw.toLowerCase() + "%");
            ps.setString(2, "%" + kw.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                kelasList.add(new Kelas(rs.getString("id"), rs.getString("nama"),
                        rs.getInt("kapasitas"), rs.getInt("semester")));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ===================== STUDENTS =====================

    private void loadStudents() {
        studentList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM users WHERE role='Student' ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                studentList.add(new Student(rs.getInt("id"), rs.getString("nama"),
                        rs.getString("email"), rs.getString("role")));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleDeleteStudent() {
        Student s = tblStudents.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("Pilih Data", "Pilih student dulu."); return; }
        Optional<ButtonType> result = confirm("Yakin hapus student " + s.getNama() + "?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
                ps.setInt(1, s.getId());
                ps.executeUpdate();
                showInfo("Berhasil", "Student dihapus.");
                loadStudents();
                handleClearStudent();
            } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
        }
    }

    @FXML private void handleClearStudent() {
        txtStudentId.clear(); txtStudentNama.clear(); txtStudentEmail.clear();
        tblStudents.getSelectionModel().clearSelection();
    }

    private void selectStudent() {
        Student s = tblStudents.getSelectionModel().getSelectedItem();
        if (s != null) {
            txtStudentId.setText(String.valueOf(s.getId()));
            txtStudentNama.setText(s.getNama());
            txtStudentEmail.setText(s.getEmail());
        }
    }

    private void searchStudents(String kw) {
        studentList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM users WHERE role='Student' AND (LOWER(nama) LIKE ? OR LOWER(email) LIKE ?) ORDER BY id")) {
            ps.setString(1, "%" + kw.toLowerCase() + "%");
            ps.setString(2, "%" + kw.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                studentList.add(new Student(rs.getInt("id"), rs.getString("nama"),
                        rs.getString("email"), rs.getString("role")));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ===================== UTILS =====================

    private void moveTo(String fxml) {
        try {
            Stage stage = (Stage) btnNavDashboard.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/loginapp/" + fxml));
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK) {{ setTitle(title); setHeaderText(null); }}.showAndWait();
    }

    private void showInfo(String title, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK) {{ setTitle(title); setHeaderText(null); }}.showAndWait();
    }

    private Optional<ButtonType> confirm(String msg) {
        return new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL) {{ setHeaderText(null); }}.showAndWait();
    }

    // ===================== MODELS =====================

    public static class RecentData {
        private final String id, type, name, status;
        public RecentData(String id, String type, String name, String status) {
            this.id=id; this.type=type; this.name=name; this.status=status;
        }
        public String getId() { return id; }
        public String getType() { return type; }
        public String getName() { return name; }
        public String getStatus() { return status; }
    }

    public static class Kelas {
        private final String id, nama;
        private final int kapasitas, semester;
        public Kelas(String id, String nama, int kapasitas, int semester) {
            this.id=id; this.nama=nama; this.kapasitas=kapasitas; this.semester=semester;
        }
        public String getId() { return id; }
        public String getNama() { return nama; }
        public int getKapasitas() { return kapasitas; }
        public int getSemester() { return semester; }
    }

    public static class Student {
        private final int id;
        private final String nama, email, role;
        public Student(int id, String nama, String email, String role) {
            this.id=id; this.nama=nama; this.email=email; this.role=role;
        }
        public int getId() { return id; }
        public String getNama() { return nama; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}
