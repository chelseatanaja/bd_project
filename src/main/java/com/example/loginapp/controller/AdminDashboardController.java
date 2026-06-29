package com.example.loginapp.controller;

import com.example.loginapp.DatabaseConnection;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AdminDashboardController — versi baru dengan:
 *  1. Dual-List assign mahasiswa (ListView Available ↔ Assigned)
 *  2. Assign dosen pengampu per kelas (ComboBox + tabel kolom Dosen)
 *     - 1 kelas hanya boleh punya 1 dosen aktif (DB constraint + konfirmasi ganti di UI)
 *  3. Tabel kelas_dosen agar DosenDashboard & StudentDashboard bisa baca dosen tiap kelas
 */
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
    @FXML private TableColumn<Kelas, String> colKelasDosen;
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();

    // === DUAL LIST MAHASISWA ===
    @FXML private ListView<StudentItem> listAvailable;               // kiri
    @FXML private ListView<StudentItem> listAssigned;                // kanan
    @FXML private TextField txtSearchMahasiswa;
    @FXML private Label lblKelasSelected;
    private final ObservableList<StudentItem> availableItems = FXCollections.observableArrayList();
    private final ObservableList<StudentItem> assignedItems  = FXCollections.observableArrayList();
    // Daftar master (semua mahasiswa), tidak berubah kecuali di-refresh
    private final List<Student> allMahasiswaList = new ArrayList<>();

    // === STUDENTS/USERS ===
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

    // === ASSIGN DOSEN ===
    @FXML private ComboBox<DosenItem> cmbAssignDosen;
    @FXML private Label lblDosenTerpilih;
    @FXML private Button btnAssignDosen;   // label dinamis: "Set Dosen" / "Ganti Dosen"

    // ===================== INITIALIZE =====================

    @FXML
    public void initialize() {
        lblSidebarNama.setText(Session.getNama() != null ? Session.getNama() : "Admin");
        lblSidebarEmail.setText(Session.getEmail() != null ? Session.getEmail() : "");

        ensureTables();

        setupDashboardColumns();
        setupKelasColumns();
        setupUsersColumns();
        setupDualList();
        setupDosenCombo();

        cmbFilterRole.setItems(FXCollections.observableArrayList("All", "Dosen", "Mahasiswa"));
        cmbFilterRole.setValue("All");
        cmbFilterRole.setOnAction(e -> filterUsers());

        tblStudents.setItems(studentList);
        tblStudents.setOnMouseClicked(e -> selectStudent());
        txtSearchStudent.textProperty().addListener((obs, o, n) -> searchStudents(n));
        applyModernUsersTableStyle();

        showView("dashboard");
        loadDashboard();
    }

    /** Buat tabel pendukung jika belum ada (idempotent). */
    private void ensureTables() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            // Tabel relasi kelas ↔ mahasiswa
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS kelas_mahasiswa (
                    kelas_id VARCHAR(50) NOT NULL,
                    user_id  INTEGER     NOT NULL,
                    PRIMARY KEY (kelas_id, user_id)
                )
            """);
            // Tabel relasi kelas ↔ dosen (satu kelas, satu dosen aktif)
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS kelas_dosen (
                    kelas_id VARCHAR(50) PRIMARY KEY,
                    dosen_id INTEGER     NOT NULL
                )
            """);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ===================== SETUP COLUMNS =====================

    private void setupDashboardColumns() {
        colRecentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRecentType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRecentName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRecentStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tblRecent.setItems(recentList);
    }

    private void setupKelasColumns() {
        colKelasId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colKelasNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colKelasKapasitas.setCellValueFactory(new PropertyValueFactory<>("kapasitas"));
        colKelasSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));

        // Jumlah mahasiswa terdaftar
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
            return new javafx.beans.property.SimpleStringProperty(count + " mahasiswa");
        });

        // Dosen pengampu — dibaca dari kelas_dosen JOIN users
        colKelasDosen.setCellValueFactory(cellData -> {
            String kelasId = cellData.getValue().getId();
            String dosenNama = "-";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT u.nama FROM kelas_dosen kd JOIN users u ON u.id = kd.dosen_id WHERE kd.kelas_id = ?")) {
                ps.setString(1, kelasId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) dosenNama = rs.getString("nama");
            } catch (Exception e) { e.printStackTrace(); }
            return new javafx.beans.property.SimpleStringProperty(dosenNama);
        });

        tblKelas.setItems(kelasList);
        tblKelas.setOnMouseClicked(e -> selectKelas());
        txtSearchKelas.textProperty().addListener((obs, o, n) -> searchKelas(n));
        applyModernKelasTableStyle();
    }

    private void setupUsersColumns() {
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colStudentEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStudentRole.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    // ===================== DUAL LIST SETUP =====================

    private void setupDualList() {
        listAvailable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listAssigned.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listAvailable.setItems(availableItems);
        listAssigned.setItems(assignedItems);

        // Filter real-time di daftar tersedia
        txtSearchMahasiswa.textProperty().addListener((obs, o, n) -> applyMahasiswaFilter(n));
    }

    /** Isi ComboBox dosen dari tabel users. */
    private void setupDosenCombo() {
        ObservableList<DosenItem> dosenList = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, nama, email FROM users WHERE role='Dosen' ORDER BY nama");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                dosenList.add(new DosenItem(rs.getInt("id"), rs.getString("nama"), rs.getString("email")));
        } catch (Exception e) { e.printStackTrace(); }
        cmbAssignDosen.setItems(dosenList);
    }

    /**
     * Muat semua mahasiswa ke master list, lalu pisahkan:
     *  - listAssigned  = yang sudah di kelas
     *  - listAvailable = sisanya
     */
    private void loadDualList(String kelasId) {
        allMahasiswaList.clear();
        availableItems.clear();
        assignedItems.clear();

        // Semua mahasiswa
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, nama, email, role FROM users WHERE role='Mahasiswa' ORDER BY nama");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                allMahasiswaList.add(new Student(rs.getInt("id"), rs.getString("nama"),
                        rs.getString("email"), rs.getString("role")));
        } catch (Exception e) { e.printStackTrace(); }

        // Yang sudah terdaftar di kelas ini
        java.util.Set<Integer> assignedIds = new java.util.HashSet<>();
        if (kelasId != null && !kelasId.isEmpty()) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT user_id FROM kelas_mahasiswa WHERE kelas_id = ?")) {
                ps.setString(1, kelasId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) assignedIds.add(rs.getInt("user_id"));
            } catch (Exception e) { e.printStackTrace(); }
        }

        for (Student s : allMahasiswaList) {
            StudentItem item = new StudentItem(s);
            if (assignedIds.contains(s.getId())) assignedItems.add(item);
            else availableItems.add(item);
        }
    }

    private void applyMahasiswaFilter(String kw) {
        if (kw == null || kw.isBlank()) {
            // Tampilkan ulang semua yang belum di assigned
            java.util.Set<Integer> assignedIds = new java.util.HashSet<>();
            for (StudentItem si : assignedItems) assignedIds.add(si.getStudent().getId());
            availableItems.setAll(
                    allMahasiswaList.stream()
                            .filter(s -> !assignedIds.contains(s.getId()))
                            .map(StudentItem::new)
                            .toList()
            );
            return;
        }
        String lower = kw.toLowerCase();
        java.util.Set<Integer> assignedIds = new java.util.HashSet<>();
        for (StudentItem si : assignedItems) assignedIds.add(si.getStudent().getId());
        availableItems.setAll(
                allMahasiswaList.stream()
                        .filter(s -> !assignedIds.contains(s.getId()))
                        .filter(s -> s.getNama().toLowerCase().contains(lower)
                                || s.getEmail().toLowerCase().contains(lower))
                        .map(StudentItem::new)
                        .toList()
        );
    }

    // ===================== DUAL LIST ACTIONS =====================

    /** Pindah yang dipilih dari Available → Assigned */
    @FXML private void handleMoveRight() {
        List<StudentItem> sel = new ArrayList<>(listAvailable.getSelectionModel().getSelectedItems());
        if (sel.isEmpty()) return;
        availableItems.removeAll(sel);
        assignedItems.addAll(sel);
    }

    /** Pindah semua dari Available → Assigned */
    @FXML private void handleMoveAllRight() {
        assignedItems.addAll(availableItems);
        availableItems.clear();
    }

    /** Pindah yang dipilih dari Assigned → Available */
    @FXML private void handleMoveLeft() {
        List<StudentItem> sel = new ArrayList<>(listAssigned.getSelectionModel().getSelectedItems());
        if (sel.isEmpty()) return;
        assignedItems.removeAll(sel);
        availableItems.addAll(sel);
    }

    /** Pindah semua dari Assigned → Available */
    @FXML private void handleMoveAllLeft() {
        availableItems.addAll(assignedItems);
        assignedItems.clear();
    }

    /**
     * Simpan hasil dual list ke DB:
     * Hapus semua kelas_mahasiswa untuk kelas ini, lalu insert yang ada di assignedItems.
     */
    @FXML private void handleSaveAssign() {
        String kelasId = txtKelasId.getText().trim();
        if (kelasId.isEmpty()) {
            showAlert("Pilih Kelas", "Pilih kelas dari tabel terlebih dahulu.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Hapus semua assign lama
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM kelas_mahasiswa WHERE kelas_id = ?")) {
                    del.setString(1, kelasId);
                    del.executeUpdate();
                }
                // Insert baru
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO kelas_mahasiswa (kelas_id, user_id) VALUES (?, ?)")) {
                    for (StudentItem si : assignedItems) {
                        ins.setString(1, kelasId);
                        ins.setInt(2, si.getStudent().getId());
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
                conn.commit();
                showInfo("Berhasil", assignedItems.size() + " mahasiswa tersimpan di kelas " + kelasId + ".");
                loadKelas(); // refresh tabel (kolom Mahasiswa)
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
                showAlert("Error", ex.getMessage());
            }
        } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
    }

    // ===================== ASSIGN DOSEN =====================

    /**
     * Set dosen untuk kelas yang sedang dipilih.
     * Aturan: 1 kelas hanya boleh punya 1 dosen aktif.
     *  - Kalau kelas belum ada dosen → assign langsung.
     *  - Kalau kelas sudah ada dosen lain → wajib konfirmasi dulu sebelum diganti.
     */
    @FXML private void handleAssignDosen() {
        String kelasId = txtKelasId.getText().trim();
        if (kelasId.isEmpty()) { showAlert("Pilih Kelas", "Pilih kelas dari tabel terlebih dahulu."); return; }
        DosenItem dosenBaru = cmbAssignDosen.getValue();
        if (dosenBaru == null) { showAlert("Pilih Dosen", "Pilih dosen dari dropdown."); return; }

        DosenItem dosenLama = getDosenForKelas(kelasId);

        if (dosenLama != null) {
            if (dosenLama.getId() == dosenBaru.getId()) {
                showInfo("Info", dosenBaru.getNama() + " memang sudah menjadi pengampu kelas ini.");
                return;
            }
            // Sudah ada dosen lain → wajib konfirmasi sebelum ganti
            Optional<ButtonType> confirm = confirm(
                    "Kelas ini sudah diampu oleh " + dosenLama.getNama() + ".\n\n" +
                            "Ganti pengampu menjadi " + dosenBaru.getNama() + "?"
            );
            if (confirm.isEmpty() || confirm.get() != ButtonType.OK) {
                return; // batal, dosen lama tetap dipertahankan
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO kelas_dosen (kelas_id, dosen_id) VALUES (?, ?) " +
                             "ON CONFLICT (kelas_id) DO UPDATE SET dosen_id = EXCLUDED.dosen_id")) {
            ps.setString(1, kelasId);
            ps.setInt(2, dosenBaru.getId());
            ps.executeUpdate();

            showInfo("Berhasil", (dosenLama == null ? "Dosen " : "Dosen diganti menjadi ")
                    + dosenBaru.getNama() + " untuk kelas " + kelasId + ".");
            lblDosenTerpilih.setText("✓ " + dosenBaru.getNama());
            btnAssignDosen.setText("🔄 Ganti Dosen");
            loadKelas();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
    }

    /** Lepas dosen dari kelas yang sedang dipilih. */
    @FXML private void handleRemoveDosen() {
        String kelasId = txtKelasId.getText().trim();
        if (kelasId.isEmpty()) { showAlert("Pilih Kelas", "Pilih kelas dari tabel terlebih dahulu."); return; }
        Optional<ButtonType> confirm = confirm("Lepas dosen dari kelas " + kelasId + "?");
        if (confirm.isPresent() && confirm.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM kelas_dosen WHERE kelas_id = ?")) {
                ps.setString(1, kelasId);
                ps.executeUpdate();
                showInfo("Berhasil", "Dosen dilepas dari kelas.");
                lblDosenTerpilih.setText("");
                btnAssignDosen.setText("👨‍🏫 Set Dosen");
                cmbAssignDosen.setValue(null);
                loadKelas();
            } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
        }
    }

    /** Ambil dosen yang sedang mengampu kelas tertentu, null kalau belum ada. */
    private DosenItem getDosenForKelas(String kelasId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT u.id, u.nama, u.email FROM kelas_dosen kd " +
                             "JOIN users u ON u.id = kd.dosen_id WHERE kd.kelas_id = ?")) {
            ps.setString(1, kelasId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new DosenItem(rs.getInt("id"), rs.getString("nama"), rs.getString("email"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ===================== MODERN STYLING TABEL KELAS =====================

    private void applyModernKelasTableStyle() {
        tblKelas.setRowFactory(tv -> {
            TableRow<Kelas> row = new TableRow<>();
            row.itemProperty().addListener((obs, o, n) ->
                    row.setStyle(n == null ? "" : "-fx-background-color: white;"));
            row.setOnMouseEntered(e -> { if (!row.isEmpty()) row.setStyle("-fx-background-color: #F8FAFF; -fx-cursor: hand;"); });
            row.setOnMouseExited(e ->  { if (!row.isEmpty()) row.setStyle("-fx-background-color: white;"); });
            return row;
        });

        // ID badge
        colKelasId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setGraphic(null); setText(null); return; }
                Label b = new Label("#" + id);
                b.setStyle("-fx-background-color:#F1F5F9; -fx-text-fill:#475569; -fx-background-radius:6; -fx-padding:3 8; -fx-font-weight:bold; -fx-font-size:12;");
                setGraphic(b); setText(null);
            }
        });

        // Nama bold
        colKelasNama.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String nama, boolean empty) {
                super.updateItem(nama, empty);
                if (empty || nama == null) { setText(null); setStyle(""); return; }
                setText(nama);
                setStyle("-fx-font-weight:bold; -fx-text-fill:#1E3A5F; -fx-font-size:13;");
            }
        });

        // Kapasitas
        colKelasKapasitas.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); setText(null); return; }
                Label l = new Label("👥 " + val);
                l.setStyle("-fx-text-fill:#64748B; -fx-font-size:12;");
                setGraphic(l); setText(null);
            }
        });

        // Semester badge kuning
        colKelasSemester.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); setText(null); return; }
                Label b = new Label("Sem " + val);
                b.setStyle("-fx-background-color:#FEF9C3; -fx-text-fill:#A16207; -fx-background-radius:20; -fx-padding:3 10; -fx-font-weight:bold; -fx-font-size:11;");
                setGraphic(b); setText(null);
            }
        });

        // Mahasiswa badge hijau
        colKelasStudents.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); setText(null); return; }
                Label b = new Label("✓ " + val);
                b.setStyle("-fx-background-color:#DCFCE7; -fx-text-fill:#16A34A; -fx-background-radius:20; -fx-padding:3 10; -fx-font-weight:bold; -fx-font-size:11;");
                setGraphic(b); setText(null);
            }
        });

        // Dosen badge biru
        colKelasDosen.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); setText(null); return; }
                if ("-".equals(val)) {
                    Label l = new Label("—");
                    l.setStyle("-fx-text-fill:#CBD5E1; -fx-font-size:12;");
                    setGraphic(l); setText(null);
                } else {
                    Label b = new Label("👨‍🏫 " + val);
                    b.setStyle("-fx-background-color:#DBEAFE; -fx-text-fill:#1D4ED8; -fx-background-radius:20; -fx-padding:3 10; -fx-font-weight:bold; -fx-font-size:11;");
                    setGraphic(b); setText(null);
                }
            }
        });
    }

    // ===================== MODERN STYLING TABEL USERS =====================

    private void applyModernUsersTableStyle() {
        tblStudents.setRowFactory(tv -> {
            TableRow<Student> row = new TableRow<>();
            row.itemProperty().addListener((obs, o, n) ->
                    row.setStyle(n == null ? "" : "-fx-background-color: white;"));
            row.setOnMouseEntered(e -> { if (!row.isEmpty()) row.setStyle("-fx-background-color: #F8FAFF; -fx-cursor: hand;"); });
            row.setOnMouseExited(e ->  { if (!row.isEmpty()) row.setStyle("-fx-background-color: white;"); });
            return row;
        });

        colStudentId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setGraphic(null); setText(null); return; }
                Label b = new Label("#" + String.format("%02d", id));
                b.setStyle("-fx-background-color:#EEF2FF; -fx-text-fill:#4F46E5; -fx-background-radius:6; -fx-padding:3 8; -fx-font-weight:bold; -fx-font-size:12;");
                setGraphic(b); setText(null);
            }
        });
        colStudentNama.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String nama, boolean empty) {
                super.updateItem(nama, empty);
                if (empty || nama == null) { setText(null); setStyle(""); return; }
                setText(nama);
                setStyle("-fx-font-weight:bold; -fx-text-fill:#0F172A; -fx-font-size:13;");
            }
        });
        colStudentEmail.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) { setText(null); setStyle(""); return; }
                setText(email);
                setStyle("-fx-text-fill:#64748B; -fx-font-size:12;");
            }
        });
        colStudentRole.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(role);
                String color = switch (role) {
                    case "Mahasiswa" -> "-fx-background-color:#DCFCE7; -fx-text-fill:#16A34A;";
                    case "Dosen"     -> "-fx-background-color:#DBEAFE; -fx-text-fill:#2563EB;";
                    default          -> "-fx-background-color:#F1F5F9; -fx-text-fill:#64748B;";
                };
                b.setStyle(color + "-fx-background-radius:20; -fx-padding:3 12; -fx-font-weight:bold; -fx-font-size:11;");
                setGraphic(b); setText(null);
            }
        });
    }

    // ===================== NAV =====================

    @FXML private void handleNavDashboard() { showView("dashboard"); loadDashboard(); setActiveNav(btnNavDashboard); }
    @FXML private void handleNavKelas()     { showView("kelas"); loadKelas(); setupDosenCombo(); setActiveNav(btnNavKelas); }
    @FXML private void handleNavStudents()  { showView("students"); loadUsers("All"); setActiveNav(btnNavStudents); }
    @FXML private void handleLogout()       { Session.clearSession(); moveTo("login.fxml"); }

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
        String activeStyle = "-fx-background-color: linear-gradient(to right,#0A1680,#2563EB); -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:12; -fx-alignment:CENTER_LEFT; -fx-cursor:hand;";
        for (Button b : new Button[]{btnNavDashboard, btnNavKelas, btnNavStudents}) b.setStyle(inactive);
        active.setStyle(activeStyle);
    }

    // ===================== DASHBOARD =====================

    private void loadDashboard() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {

            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM kelas");
            if (rs.next()) lblTotalKelas.setText(String.valueOf(rs.getInt(1)));

            rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE role != 'Admin'");
            if (rs.next()) lblTotalUser.setText(String.valueOf(rs.getInt(1)));

            rs = st.executeQuery("SELECT COUNT(*) FROM kelas_mahasiswa");
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
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, nama, kapasitas, semester FROM kelas ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                kelasList.add(new Kelas(rs.getString("id"), rs.getString("nama"),
                        rs.getInt("kapasitas"), rs.getInt("semester")));
        } catch (SQLException e) { e.printStackTrace(); }
        tblKelas.setItems(kelasList);
    }

    @FXML private void handleAddKelas() {
        if (txtKelasId.getText().isEmpty() || txtKelasNama.getText().isEmpty()) {
            showAlert("Gagal", "ID dan Nama Kelas wajib diisi."); return;
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
            loadKelas(); handleClearKelas();
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
            loadKelas(); handleClearKelas();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
    }

    @FXML private void handleDeleteKelas() {
        if (txtKelasId.getText().isEmpty()) { showAlert("Pilih Data", "Pilih kelas dulu."); return; }
        Optional<ButtonType> res = confirm("Yakin hapus kelas ini? Data assign mahasiswa dan dosen juga dihapus.");
        if (res.isPresent() && res.get() == ButtonType.OK) {
            String id = txtKelasId.getText();
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement d1 = conn.prepareStatement("DELETE FROM kelas_mahasiswa WHERE kelas_id=?");
                     PreparedStatement d2 = conn.prepareStatement("DELETE FROM kelas_dosen WHERE kelas_id=?");
                     PreparedStatement d3 = conn.prepareStatement("DELETE FROM kelas WHERE id=?")) {
                    d1.setString(1, id); d1.executeUpdate();
                    d2.setString(1, id); d2.executeUpdate();
                    d3.setString(1, id); d3.executeUpdate();
                    conn.commit();
                } catch (Exception ex) { conn.rollback(); throw ex; }
                showInfo("Berhasil", "Kelas dihapus.");
                loadKelas(); handleClearKelas();
            } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
        }
    }

    @FXML private void handleClearKelas() {
        txtKelasId.clear(); txtKelasNama.clear(); txtKelasKapasitas.clear(); txtKelasSemester.clear();
        tblKelas.getSelectionModel().clearSelection();
        availableItems.clear(); assignedItems.clear();
        lblKelasSelected.setText("Pilih kelas dari tabel di atas");
        lblDosenTerpilih.setText(""); cmbAssignDosen.setValue(null);
        btnAssignDosen.setText("👨‍🏫 Set Dosen");
    }

    @FXML private void handleRefreshKelas() {
        loadKelas(); setupDosenCombo(); txtSearchKelas.clear();
        availableItems.clear(); assignedItems.clear();
        lblKelasSelected.setText("Pilih kelas dari tabel di atas");
    }

    private void selectKelas() {
        Kelas s = tblKelas.getSelectionModel().getSelectedItem();
        if (s == null) return;

        txtKelasId.setText(s.getId());
        txtKelasNama.setText(s.getNama());
        txtKelasKapasitas.setText(String.valueOf(s.getKapasitas()));
        txtKelasSemester.setText(String.valueOf(s.getSemester()));

        lblKelasSelected.setText("Kelas: " + s.getNama() + " (" + s.getId() + ")");

        // Muat dual list untuk kelas ini
        loadDualList(s.getId());

        // Muat dosen yang sudah di-assign
        lblDosenTerpilih.setText("");
        cmbAssignDosen.setValue(null);
        btnAssignDosen.setText("👨‍🏫 Set Dosen"); // default dulu

        DosenItem dosenAktif = getDosenForKelas(s.getId());
        if (dosenAktif != null) {
            for (DosenItem di : cmbAssignDosen.getItems()) {
                if (di.getId() == dosenAktif.getId()) { cmbAssignDosen.setValue(di); break; }
            }
            lblDosenTerpilih.setText("✓ " + dosenAktif.getNama() + " (" + dosenAktif.getEmail() + ")");
            btnAssignDosen.setText("🔄 Ganti Dosen");
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

    private void loadUsers(String roleFilter) {
        studentList.clear();
        try {
            String query = switch (roleFilter) {
                case "Dosen"     -> "SELECT * FROM users WHERE role = 'Dosen' ORDER BY id";
                case "Mahasiswa" -> "SELECT * FROM users WHERE role = 'Mahasiswa' ORDER BY id";
                default          -> "SELECT * FROM users WHERE role != 'Admin' ORDER BY id";
            };
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    studentList.add(new Student(rs.getInt("id"), rs.getString("nama"),
                            rs.getString("email"), rs.getString("role")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void filterUsers() {
        String selected = cmbFilterRole.getValue();
        loadUsers(selected != null ? selected : "All");
    }

    @FXML private void handleDeleteStudent() {
        Student s = tblStudents.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("Pilih Data", "Pilih user dulu."); return; }
        Optional<ButtonType> res = confirm("Yakin hapus user " + s.getNama() + "?");
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
                ps.setInt(1, s.getId());
                ps.executeUpdate();
                showInfo("Berhasil", "User dihapus.");
                filterUsers(); handleClearStudent();
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
        String roleCondition = switch (roleFilter) {
            case "Dosen"     -> "role = 'Dosen'";
            case "Mahasiswa" -> "role = 'Mahasiswa'";
            default          -> "role != 'Admin'";
        };
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
        public RecentData(String id, String type, String name, String status) { this.id=id; this.type=type; this.name=name; this.status=status; }
        public String getId()     { return id; }
        public String getType()   { return type; }
        public String getName()   { return name; }
        public String getStatus() { return status; }
    }

    public static class Kelas {
        private final String id, nama;
        private final int kapasitas, semester;
        public Kelas(String id, String nama, int kapasitas, int semester) { this.id=id; this.nama=nama; this.kapasitas=kapasitas; this.semester=semester; }
        public String getId()     { return id; }
        public String getNama()   { return nama; }
        public int getKapasitas() { return kapasitas; }
        public int getSemester()  { return semester; }
    }

    public static class Student {
        private final int id;
        private final String nama, email, role;
        public Student(int id, String nama, String email, String role) { this.id=id; this.nama=nama; this.email=email; this.role=role; }
        public int getId()       { return id; }
        public String getNama()  { return nama; }
        public String getEmail() { return email; }
        public String getRole()  { return role; }
    }

    /** Wrapper Student untuk ListView — toString dipakai sebagai label item. */
    public static class StudentItem {
        private final Student student;
        public StudentItem(Student student) { this.student = student; }
        public Student getStudent() { return student; }
        @Override public String toString() {
            return student.getNama() + "  ·  " + student.getEmail();
        }
    }

    /** Item dosen untuk ComboBox. */
    public static class DosenItem {
        private final int id;
        private final String nama, email;
        public DosenItem(int id, String nama, String email) { this.id=id; this.nama=nama; this.email=email; }
        public int    getId()    { return id; }
        public String getNama()  { return nama; }
        public String getEmail() { return email; }
        @Override public String toString() { return nama + "  ·  " + email; }
    }
}