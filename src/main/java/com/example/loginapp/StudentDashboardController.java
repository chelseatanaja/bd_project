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

public class StudentDashboardController {

    // === SIDEBAR ===
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavTugas;
    @FXML private Button btnNavMateri;
    @FXML private Button btnNavPengumuman;
    @FXML private Label lblSidebarNama;
    @FXML private Label lblSidebarEmail;

    // === VIEWS ===
    @FXML private VBox viewDashboard;
    @FXML private VBox viewTugas;
    @FXML private VBox viewMateri;
    @FXML private VBox viewPengumuman;

    // === DASHBOARD ===
    @FXML private Label lblWelcome;
    @FXML private Label lblMyClasses;
    @FXML private Label lblUpcomingTasks;
    @FXML private Label lblPengumuman;
    @FXML private TableView<StudentAssignment> tblDashboard;
    @FXML private TableColumn<StudentAssignment, String> colDashJudul;
    @FXML private TableColumn<StudentAssignment, String> colDashKelas;
    @FXML private TableColumn<StudentAssignment, String> colDashDeadline;
    @FXML private TableColumn<StudentAssignment, String> colDashStatus;

    // === TUGAS ===
    @FXML private ComboBox<String> cmbPilihTugas;
    @FXML private TextField txtFileUrl;
    @FXML private TextField txtSearchTugas;
    @FXML private TableView<StudentAssignment> tblStudentAssignment;
    @FXML private TableColumn<StudentAssignment, Integer> colAssignmentId;
    @FXML private TableColumn<StudentAssignment, String> colAssignmentNama;
    @FXML private TableColumn<StudentAssignment, String> colKelas;
    @FXML private TableColumn<StudentAssignment, String> colDeadline;
    @FXML private TableColumn<StudentAssignment, String> colStatus;
    @FXML private TableColumn<StudentAssignment, String> colNilai;
    private final ObservableList<StudentAssignment> assignmentList = FXCollections.observableArrayList();

    // === MATERI ===
    @FXML private TableView<Materi> tblMateri;
    @FXML private TableColumn<Materi, Integer> colMateriId;
    @FXML private TableColumn<Materi, String> colMateriJudul;
    @FXML private TableColumn<Materi, String> colMateriKelas;
    @FXML private TableColumn<Materi, String> colMateriIsi;
    private final ObservableList<Materi> materiList = FXCollections.observableArrayList();

    // === PENGUMUMAN ===
    @FXML private TableView<Pengumuman> tblPengumuman;
    @FXML private TableColumn<Pengumuman, String> colPengJudul;
    @FXML private TableColumn<Pengumuman, String> colPengIsi;
    @FXML private TableColumn<Pengumuman, String> colPengKelas;
    @FXML private TableColumn<Pengumuman, String> colPengTgl;
    private final ObservableList<Pengumuman> pengumumanList = FXCollections.observableArrayList();

    // tugas id map for submission
    private final java.util.Map<String, Integer> tugasIdMap = new java.util.LinkedHashMap<>();

    @FXML
    public void initialize() {
        lblSidebarNama.setText(Session.getNama() != null ? Session.getNama() : "Student");
        lblSidebarEmail.setText(Session.getEmail() != null ? Session.getEmail() : "");
        lblWelcome.setText("Halo, " + (Session.getNama() != null ? Session.getNama() : "Student") + "!");

        setupTugasTable();
        setupMateriTable();
        setupPengumumanTable();
        setupDashboardTable();

        showView("dashboard");
        loadDashboard();
    }

    // ===================== NAV =====================

    @FXML private void handleNavDashboard() { showView("dashboard"); loadDashboard(); setActive(btnNavDashboard); }
    @FXML private void handleNavTugas()     { showView("tugas"); loadTugas(); setActive(btnNavTugas); }
    @FXML private void handleNavMateri()    { showView("materi"); loadMateri(); setActive(btnNavMateri); }
    @FXML private void handleNavPengumuman(){ showView("pengumuman"); loadPengumuman(); setActive(btnNavPengumuman); }

    @FXML private void handleLogout() {
        Session.clearSession();
        moveTo("login.fxml");
    }

    private void showView(String v) {
        viewDashboard.setVisible(false); viewTugas.setVisible(false);
        viewMateri.setVisible(false); viewPengumuman.setVisible(false);
        switch (v) {
            case "dashboard"   -> viewDashboard.setVisible(true);
            case "tugas"       -> viewTugas.setVisible(true);
            case "materi"      -> viewMateri.setVisible(true);
            case "pengumuman"  -> viewPengumuman.setVisible(true);
        }
    }

    private void setActive(Button b) {
        String off = "-fx-background-color:transparent; -fx-text-fill:#CBD5E1; -fx-font-size:14; -fx-alignment:CENTER_LEFT; -fx-cursor:hand; -fx-background-radius:12;";
        String on  = "-fx-background-color:linear-gradient(to right,#4F46E5,#2563EB); -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:12; -fx-alignment:CENTER_LEFT; -fx-cursor:hand;";
        for (Button btn : new Button[]{btnNavDashboard, btnNavTugas, btnNavMateri, btnNavPengumuman})
            btn.setStyle(off);
        b.setStyle(on);
    }

    // ===================== SETUP TABLES =====================

    private void setupDashboardTable() {
        colDashJudul.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colDashKelas.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        colDashDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        colDashStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupTugasTable() {
        colAssignmentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAssignmentNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colKelas.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colNilai.setCellValueFactory(new PropertyValueFactory<>("nilai"));
        tblStudentAssignment.setItems(assignmentList);
        txtSearchTugas.textProperty().addListener((obs, o, n) -> filterTugas(n));
    }

    private void setupMateriTable() {
        colMateriId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMateriJudul.setCellValueFactory(new PropertyValueFactory<>("judul"));
        colMateriKelas.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        colMateriIsi.setCellValueFactory(new PropertyValueFactory<>("isi"));
        tblMateri.setItems(materiList);
    }

    private void setupPengumumanTable() {
        colPengJudul.setCellValueFactory(new PropertyValueFactory<>("judul"));
        colPengIsi.setCellValueFactory(new PropertyValueFactory<>("isi"));
        colPengKelas.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        colPengTgl.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        tblPengumuman.setItems(pengumumanList);
    }

    // ===================== LOAD DATA =====================

    private void loadDashboard() {
        int userId = Session.getUserId();
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            // Total tugas aktif
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM assignment");
            int totalTugas = rs.next() ? rs.getInt(1) : 0;
            lblMyClasses.setText(String.valueOf(totalTugas));

            // Sudah dikumpul
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM submission WHERE user_id=?");
            ps.setInt(1, userId); rs = ps.executeQuery();
            lblUpcomingTasks.setText(rs.next() ? String.valueOf(rs.getInt(1)) : "0");

            // Pengumuman
            try {
                rs = st.executeQuery("SELECT COUNT(*) FROM pengumuman");
                lblPengumuman.setText(rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            } catch (Exception ignored) { lblPengumuman.setText("0"); }

            // Dashboard table - tugas + status kumpul
            ObservableList<StudentAssignment> dash = FXCollections.observableArrayList();
            ps = conn.prepareStatement(
                    "SELECT a.id, a.judul, k.nama AS kelas, a.deadline, " +
                            "CASE WHEN s.id IS NOT NULL THEN 'Sudah Kumpul' ELSE 'Belum Kumpul' END AS status, " +
                            "COALESCE(s.nilai, '-') AS nilai " +
                            "FROM assignment a " +
                            "LEFT JOIN kelas k ON a.kelas_id = k.id " +
                            "LEFT JOIN submission s ON s.assignment_id = a.id AND s.user_id = ? " +
                            "ORDER BY a.deadline");
            ps.setInt(1, userId); rs = ps.executeQuery();
            while (rs.next())
                dash.add(new StudentAssignment(rs.getInt("id"), rs.getString("judul"),
                        nvl(rs.getString("kelas")), nvl(rs.getString("deadline")),
                        nvl(rs.getString("status")), nvl(rs.getString("nilai"))));
            tblDashboard.setItems(dash);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadTugas() {
        assignmentList.clear();
        tugasIdMap.clear();
        int userId = Session.getUserId();
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT a.id, a.judul, k.nama AS kelas, a.deadline, " +
                            "CASE WHEN s.id IS NOT NULL THEN 'Sudah Kumpul' ELSE 'Belum Kumpul' END AS status, " +
                            "COALESCE(s.nilai, '-') AS nilai " +
                            "FROM assignment a " +
                            "LEFT JOIN kelas k ON a.kelas_id = k.id " +
                            "LEFT JOIN submission s ON s.assignment_id = a.id AND s.user_id = ? " +
                            "ORDER BY a.deadline");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            ObservableList<String> tugasOptions = FXCollections.observableArrayList();
            while (rs.next()) {
                int id = rs.getInt("id");
                String judul = rs.getString("judul");
                assignmentList.add(new StudentAssignment(id, judul,
                        nvl(rs.getString("kelas")), nvl(rs.getString("deadline")),
                        nvl(rs.getString("status")), nvl(rs.getString("nilai"))));
                tugasOptions.add(judul);
                tugasIdMap.put(judul, id);
            }
            cmbPilihTugas.setItems(tugasOptions);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadMateri() {
        materiList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT m.id, m.judul, k.nama AS kelas, m.isi FROM materi m " +
                             "LEFT JOIN kelas k ON m.kelas_id = k.id ORDER BY m.id")) {
            while (rs.next())
                materiList.add(new Materi(rs.getInt("id"), rs.getString("judul"),
                        nvl(rs.getString("kelas")), nvl(rs.getString("isi"))));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPengumuman() {
        pengumumanList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT p.judul, p.isi, k.nama AS kelas, p.tanggal FROM pengumuman p " +
                             "LEFT JOIN kelas k ON p.kelas_id = k.id ORDER BY p.id DESC")) {
            while (rs.next())
                pengumumanList.add(new Pengumuman(rs.getString("judul"),
                        nvl(rs.getString("isi")), nvl(rs.getString("kelas")), nvl(rs.getString("tanggal"))));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ===================== HANDLERS =====================

    @FXML private void handleKumpulTugas() {
        String judul = cmbPilihTugas.getValue();
        String fileUrl = txtFileUrl.getText().trim();
        if (judul == null) { showAlert("Pilih Tugas", "Pilih tugas yang ingin dikumpulkan."); return; }
        if (fileUrl.isEmpty()) { showAlert("Isi Link", "Isi link atau nama file terlebih dahulu."); return; }

        int assignmentId = tugasIdMap.getOrDefault(judul, -1);
        if (assignmentId == -1) { showAlert("Error", "Tugas tidak ditemukan."); return; }

        int userId = Session.getUserId();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Cek apakah sudah pernah kumpul
            PreparedStatement cek = conn.prepareStatement(
                    "SELECT id FROM submission WHERE user_id=? AND assignment_id=?");
            cek.setInt(1, userId); cek.setInt(2, assignmentId);
            ResultSet rs = cek.executeQuery();

            if (rs.next()) {
                // Update
                PreparedStatement upd = conn.prepareStatement(
                        "UPDATE submission SET file_url=?, tanggal_submit=CURRENT_DATE WHERE user_id=? AND assignment_id=?");
                upd.setString(1, fileUrl); upd.setInt(2, userId); upd.setInt(3, assignmentId);
                upd.executeUpdate();
                showInfo("Berhasil", "Tugas berhasil diperbarui.");
            } else {
                // Insert
                PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO submission(user_id, assignment_id, file_url, tanggal_submit) VALUES (?,?,?,CURRENT_DATE)");
                ins.setInt(1, userId); ins.setInt(2, assignmentId); ins.setString(3, fileUrl);
                ins.executeUpdate();
                showInfo("Berhasil", "Tugas berhasil dikumpulkan!");
            }
            txtFileUrl.clear(); cmbPilihTugas.setValue(null);
            loadTugas();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
    }

    private void filterTugas(String kw) {
        if (kw == null || kw.isEmpty()) { tblStudentAssignment.setItems(assignmentList); return; }
        ObservableList<StudentAssignment> f = FXCollections.observableArrayList();
        for (StudentAssignment a : assignmentList)
            if (a.getNama().toLowerCase().contains(kw.toLowerCase()) ||
                    a.getKelas().toLowerCase().contains(kw.toLowerCase()))
                f.add(a);
        tblStudentAssignment.setItems(f);
    }

    // ===================== UTILS =====================

    private String nvl(String s) { return s != null ? s : "-"; }

    private void moveTo(String fxml) {
        try {
            Stage stage = (Stage) btnNavDashboard.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/loginapp/" + fxml));
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String t, String m) {
        new Alert(Alert.AlertType.WARNING, m, ButtonType.OK) {{ setTitle(t); setHeaderText(null); }}.showAndWait();
    }
    private void showInfo(String t, String m) {
        new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK) {{ setTitle(t); setHeaderText(null); }}.showAndWait();
    }

    // ===================== MODELS =====================

    public static class StudentAssignment {
        private final int id; private final String nama, kelas, deadline, status, nilai;
        public StudentAssignment(int id, String nama, String kelas, String deadline, String status, String nilai) {
            this.id=id; this.nama=nama; this.kelas=kelas; this.deadline=deadline; this.status=status; this.nilai=nilai;
        }
        public int getId() { return id; } public String getNama() { return nama; }
        public String getKelas() { return kelas; } public String getDeadline() { return deadline; }
        public String getStatus() { return status; } public String getNilai() { return nilai; }
    }

    public static class Materi {
        private final int id; private final String judul, kelas, isi;
        public Materi(int id, String judul, String kelas, String isi) { this.id=id; this.judul=judul; this.kelas=kelas; this.isi=isi; }
        public int getId() { return id; } public String getJudul() { return judul; }
        public String getKelas() { return kelas; } public String getIsi() { return isi; }
    }

    public static class Pengumuman {
        private final String judul, isi, kelas, tanggal;
        public Pengumuman(String judul, String isi, String kelas, String tanggal) {
            this.judul=judul; this.isi=isi; this.kelas=kelas; this.tanggal=tanggal;
        }
        public String getJudul() { return judul; } public String getIsi() { return isi; }
        public String getKelas() { return kelas; } public String getTanggal() { return tanggal; }
    }
}
