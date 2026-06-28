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
    @FXML private TableColumn<Kelas, String> colKelasStudents;
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();

    // cmbAssignStudent dari FXML hanya sebagai placeholder MenuButton
    @FXML private MenuButton cmbAssignStudent;
    // ini yang BENAR-BENAR dipakai di seluruh kode
    private CustomCheckComboBox checkCombo;

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
    @FXML private ComboBox<String> cmbFilterRole;
    private final ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        lblSidebarNama.setText(Session.getNama() != null ? Session.getNama() : "Admin");
        lblSidebarEmail.setText(Session.getEmail() != null ? Session.getEmail() : "");

        // === SETUP KOLOM RECENT ===
        colRecentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRecentType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRecentName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRecentStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // === SETUP KOLOM KELAS ===
        colKelasId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colKelasNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colKelasKapasitas.setCellValueFactory(new PropertyValueFactory<>("kapasitas"));
        colKelasSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colKelasStudents.setCellValueFactory(cellData -> {
            String kelasId = cellData.getValue().getId();
            int count = 0;
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT COUNT(*) FROM kelas_mahasiswa WHERE kelas_id = ?")) {
                ps.setString(1, kelasId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) count = rs.getInt(1);
            } catch (Exception e) { e.printStackTrace(); }
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(count));
        });
        tblKelas.setItems(kelasList);
        tblKelas.setOnMouseClicked(e -> selectKelas());
        txtSearchKelas.textProperty().addListener((obs, o, n) -> searchKelas(n));
        applyModernKelasTableStyle();

        // === SETUP KOLOM STUDENTS ===
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colStudentEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStudentRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        tblStudents.setItems(studentList);
        tblStudents.setOnMouseClicked(e -> selectStudent());
        txtSearchStudent.textProperty().addListener((obs, o, n) -> searchStudents(n));
        applyModernUsersTableStyle();

        // === FILTER ROLE ===
        cmbFilterRole.setItems(FXCollections.observableArrayList("All", "Dosen", "Mahasiswa"));
        cmbFilterRole.setValue("All");
        cmbFilterRole.setOnAction(e -> filterUsers());

        // === SWAP MenuButton → CustomCheckComboBox ===
        checkCombo = new CustomCheckComboBox();
        checkCombo.setMaxWidth(Double.MAX_VALUE);
        checkCombo.setPrefHeight(42);
        checkCombo.setStyle(
                "-fx-background-color:white; -fx-border-color:#E2E8F0; " +
                        "-fx-border-radius:8; -fx-background-radius:8; -fx-text-fill:#334155;"
        );
        VBox parentVBox = (VBox) cmbAssignStudent.getParent();
        int idx = parentVBox.getChildren().indexOf(cmbAssignStudent);
        parentVBox.getChildren().set(idx, checkCombo);

        showView("dashboard");
        loadDashboard();
    }

    // ===================== MODERN STYLING TABEL KELAS =====================

    private void applyModernKelasTableStyle() {

        tblKelas.setRowFactory(tv -> {
            TableRow<Kelas> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) row.setStyle("");
                else row.setStyle("-fx-background-color: white;");
            });
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) row.setStyle("-fx-background-color: #F8FAFF; -fx-cursor: hand;");
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty()) row.setStyle("-fx-background-color: white;");
            });
            return row;
        });

        // Kolom ID — badge abu
        colKelasId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setGraphic(null); setText(null); }
                else {
                    Label badge = new Label("#" + id);
                    badge.setStyle(
                            "-fx-background-color:#F1F5F9; -fx-text-fill:#475569; " +
                                    "-fx-background-radius:6; -fx-padding:3 8; " +
                                    "-fx-font-weight:bold; -fx-font-size:12;"
                    );
                    setGraphic(badge); setText(null);
                }
            }
        });

        // Kolom Nama Kelas — bold biru gelap
        colKelasNama.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String nama, boolean empty) {
                super.updateItem(nama, empty);
                if (empty || nama == null) { setText(null); setStyle(""); }
                else {
                    setText(nama);
                    setStyle("-fx-font-weight:bold; -fx-text-fill:#1E3A5F; -fx-font-size:13;");
                }
            }
        });

        // Kolom Kapasitas — dengan icon
        colKelasKapasitas.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); setText(null); }
                else {
                    Label lbl = new Label("👥 " + val);
                    lbl.setStyle("-fx-text-fill:#64748B; -fx-font-size:12;");
                    setGraphic(lbl); setText(null);
                }
            }
        });

        // Kolom Semester — badge kuning
        colKelasSemester.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); setText(null); }
                else {
                    Label badge = new Label("Sem " + val);
                    badge.setStyle(
                            "-fx-background-color:#FEF9C3; -fx-text-fill:#A16207; " +
                                    "-fx-background-radius:20; -fx-padding:3 10; " +
                                    "-fx-font-weight:bold; -fx-font-size:11;"
                    );
                    setGraphic(badge); setText(null);
                }
            }
        });

        // Kolom Mahasiswa Terdaftar — badge hijau
        colKelasStudents.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); setText(null); }
                else {
                    Label badge = new Label("✓ " + val + " mahasiswa");
                    badge.setStyle(
                            "-fx-background-color:#DCFCE7; -fx-text-fill:#16A34A; " +
                                    "-fx-background-radius:20; -fx-padding:3 10; " +
                                    "-fx-font-weight:bold; -fx-font-size:11;"
                    );
                    setGraphic(badge); setText(null);
                }
            }
        });
    }

    // ===================== MODERN STYLING TABEL USERS =====================

    private void applyModernUsersTableStyle() {

        tblStudents.setRowFactory(tv -> {
            TableRow<Student> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) row.setStyle("");
                else row.setStyle("-fx-background-color: white;");
            });
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) row.setStyle("-fx-background-color: #F8FAFF; -fx-cursor: hand;");
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty()) row.setStyle("-fx-background-color: white;");
            });
            return row;
        });

        // Kolom ID — badge ungu
        colStudentId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setGraphic(null); setText(null); }
                else {
                    Label badge = new Label("#" + String.format("%02d", id));
                    badge.setStyle(
                            "-fx-background-color:#EEF2FF; -fx-text-fill:#4F46E5; " +
                                    "-fx-background-radius:6; -fx-padding:3 8; " +
                                    "-fx-font-weight:bold; -fx-font-size:12;"
                    );
                    setGraphic(badge); setText(null);
                }
            }
        });

        // Kolom Nama — bold hitam
        colStudentNama.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String nama, boolean empty) {
                super.updateItem(nama, empty);
                if (empty || nama == null) { setText(null); setStyle(""); }
                else {
                    setText(nama);
                    setStyle("-fx-font-weight:bold; -fx-text-fill:#0F172A; -fx-font-size:13;");
                }
            }
        });

        // Kolom Email — abu-abu tipis
        colStudentEmail.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) { setText(null); setStyle(""); }
                else {
                    setText(email);
                    setStyle("-fx-text-fill:#64748B; -fx-font-size:12;");
                }
            }
        });

        // Kolom Role — badge warna berbeda (fix: Mahasiswa bukan Student)
        colStudentRole.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) { setGraphic(null); setText(null); }
                else {
                    Label badge = new Label(role);
                    String color = switch (role) {
                        case "Mahasiswa" -> "-fx-background-color:#DCFCE7; -fx-text-fill:#16A34A;";
                        case "Dosen"     -> "-fx-background-color:#DBEAFE; -fx-text-fill:#2563EB;";
                        default          -> "-fx-background-color:#F1F5F9; -fx-text-fill:#64748B;";
                    };
                    badge.setStyle(
                            color +
                                    "-fx-background-radius:20; -fx-padding:3 12; " +
                                    "-fx-font-weight:bold; -fx-font-size:11;"
                    );
                    setGraphic(badge); setText(null);
                }
            }
        });
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
        loadStudentDropdown();
        setActiveNav(btnNavKelas);
    }

    @FXML private void handleNavStudents() {
        showView("students");
        loadUsers("All");
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
            case "kelas"     -> viewKelas.setVisible(true);
            case "students"  -> viewStudents.setVisible(true);
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
            rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE role='Mahasiswa'"); // fix
            if (rs.next()) lblTotalUser.setText(String.valueOf(rs.getInt(1)));
            rs = st.executeQuery("SELECT COUNT(*) FROM assignment");
            if (rs.next()) lblTotalAssignment.setText(String.valueOf(rs.getInt(1)));
            recentList.clear();
            rs = st.executeQuery("SELECT id, nama FROM kelas ORDER BY id DESC LIMIT 5");
            while (rs.next())
                recentList.add(new RecentData(rs.getString("id"), "Kelas", rs.getString("nama"), "Active"));
            tblRecent.setItems(recentList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ===================== KELAS =====================

    private void loadKelas() {
        kelasList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, nama, kapasitas, semester FROM kelas ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                kelasList.add(new Kelas(
                        rs.getString("id"),
                        rs.getString("nama"),
                        rs.getInt("kapasitas"),
                        rs.getInt("semester")
                ));
            }
            tblKelas.setItems(kelasList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadStudentDropdown() {
        java.util.List<Student> list = new java.util.ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, nama, email, role FROM users WHERE role='Mahasiswa' ORDER BY nama"); // fix
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new Student(rs.getInt("id"), rs.getString("nama"),
                        rs.getString("email"), rs.getString("role")));
        } catch (Exception e) { e.printStackTrace(); }
        checkCombo.setStudents(list);
    }

    @FXML private void handleAssignStudent() {
        if (txtKelasId.getText().isEmpty()) {
            showAlert("Pilih Kelas", "Pilih kelas dari tabel dulu.");
            return;
        }
        java.util.List<Student> selected = checkCombo.getSelectedStudents();
        if (selected.isEmpty()) {
            showAlert("Pilih Mahasiswa", "Pilih minimal satu mahasiswa.");
            return;
        }

        // Buat tabel jika belum ada
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS kelas_mahasiswa (
                    kelas_id VARCHAR(50),
                    user_id INTEGER,
                    PRIMARY KEY (kelas_id, user_id)
                )
            """);
        } catch (Exception e) { e.printStackTrace(); }

        // Insert batch
        int berhasil = 0, sudahAda = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO kelas_mahasiswa (kelas_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
            for (Student s : selected) {
                ps.setString(1, txtKelasId.getText().trim());
                ps.setInt(2, s.getId());
                int rows = ps.executeUpdate();
                if (rows > 0) berhasil++;
                else sudahAda++;
            }
        } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); return; }

        String msg = berhasil + " mahasiswa berhasil di-assign.";
        if (sudahAda > 0) msg += "\n" + sudahAda + " sudah terdaftar sebelumnya (dilewati).";
        showInfo("Selesai", msg);

        checkCombo.clearSelection();
        loadKelas();
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
        if (checkCombo != null) checkCombo.clearSelection();
    }

    @FXML private void handleRefreshKelas() {
        loadKelas();
        loadStudentDropdown();
        txtSearchKelas.clear();
        if (checkCombo != null) checkCombo.clearSelection();
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

    // ===================== USERS =====================

    private void loadStudents() { loadUsers("All"); }

    private void loadUsers(String roleFilter) {
        studentList.clear();
        try {
            String query = "All".equals(roleFilter) ?
                    "SELECT * FROM users WHERE role != 'Admin' ORDER BY id" :
                    "Dosen".equals(roleFilter) ?
                            "SELECT * FROM users WHERE role = 'Dosen' ORDER BY id" :
                            "SELECT * FROM users WHERE role = 'Mahasiswa' ORDER BY id"; // fix
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                studentList.add(new Student(rs.getInt("id"), rs.getString("nama"),
                        rs.getString("email"), rs.getString("role")));
            ps.close(); conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void filterUsers() {
        String selected = cmbFilterRole.getValue();
        loadUsers(selected != null ? selected : "All");
    }

    @FXML private void handleDeleteStudent() {
        Student s = tblStudents.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("Pilih Data", "Pilih user dulu."); return; }
        Optional<ButtonType> result = confirm("Yakin hapus user " + s.getNama() + "?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
                ps.setInt(1, s.getId());
                ps.executeUpdate();
                showInfo("Berhasil", "User dihapus.");
                filterUsers();
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
        String roleFilter = cmbFilterRole.getValue() != null ? cmbFilterRole.getValue() : "All";
        String roleCondition = "All".equals(roleFilter) ? "role != 'Admin'" :
                "Dosen".equals(roleFilter) ? "role = 'Dosen'" : "role = 'Mahasiswa'"; // fix
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM users WHERE " + roleCondition +
                             " AND (LOWER(nama) LIKE ? OR LOWER(email) LIKE ?) ORDER BY id")) {
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
        public String getId()     { return id; }
        public String getType()   { return type; }
        public String getName()   { return name; }
        public String getStatus() { return status; }
    }

    public static class Kelas {
        private final String id, nama;
        private final int kapasitas, semester;
        public Kelas(String id, String nama, int kapasitas, int semester) {
            this.id=id; this.nama=nama; this.kapasitas=kapasitas; this.semester=semester;
        }
        public String getId()     { return id; }
        public String getNama()   { return nama; }
        public int getKapasitas() { return kapasitas; }
        public int getSemester()  { return semester; }
    }

    public static class Student {
        private final int id;
        private final String nama, email, role;
        public Student(int id, String nama, String email, String role) {
            this.id=id; this.nama=nama; this.email=email; this.role=role;
        }
        public int getId()       { return id; }
        public String getNama()  { return nama; }
        public String getEmail() { return email; }
        public String getRole()  { return role; }
    }

    // ===================== CUSTOM CHECK COMBO BOX =====================

    public static class CustomCheckComboBox extends MenuButton {
        private final java.util.List<CheckBox> checkBoxes = new java.util.ArrayList<>();
        private final java.util.List<Student> students = new java.util.ArrayList<>();

        public CustomCheckComboBox() {
            setText("Pilih Mahasiswa...");
        }

        public void setStudents(java.util.List<Student> list) {
            checkBoxes.clear();
            students.clear();
            getItems().clear();

            for (Student s : list) {
                CheckBox cb = new CheckBox(s.getNama() + " (" + s.getEmail() + ")");
                cb.setStyle("-fx-padding: 4 8;");
                CustomMenuItem item = new CustomMenuItem(cb, false);
                getItems().add(item);
                checkBoxes.add(cb);
                students.add(s);
                cb.selectedProperty().addListener((obs, o, n) -> updateText());
            }
            updateText();
        }

        private void updateText() {
            long count = checkBoxes.stream().filter(CheckBox::isSelected).count();
            setText(count == 0 ? "Pilih Mahasiswa..." : count + " mahasiswa dipilih");
        }

        public java.util.List<Student> getSelectedStudents() {
            java.util.List<Student> result = new java.util.ArrayList<>();
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isSelected()) result.add(students.get(i));
            }
            return result;
        }

        public void clearSelection() {
            checkBoxes.forEach(cb -> cb.setSelected(false));
            updateText();
        }
    }
}