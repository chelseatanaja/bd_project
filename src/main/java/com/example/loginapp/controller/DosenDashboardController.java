package com.example.loginapp.controller;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;


import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;


public class DosenDashboardController {


    // === SIDEBAR ===
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavTugas;
    @FXML private Button btnNavMateri;
    @FXML private Button btnNavSubmission;
    @FXML private Button btnNavPengumuman;
    @FXML private Label lblSidebarNama;
    @FXML private Label lblSidebarEmail;


    // === VIEWS ===
    @FXML private VBox viewDashboard;
    @FXML private VBox viewTugas;
    @FXML private VBox viewMateri;
    @FXML private VBox viewSubmission;
    @FXML private VBox viewPengumuman;


    // === DASHBOARD ===
    @FXML private Label lblTotalAssignment;
    @FXML private Label lblTotalMateri;
    @FXML private Label lblTotalSubmission;
    @FXML private Label lblTotalPengumuman;
    @FXML private FlowPane flowKelas;          // card grid untuk kelas


    // === TUGAS ===
    @FXML private TextField txtTugasJudul;
    @FXML private TextArea txtTugasDeskripsi;
    @FXML private ComboBox<String> cmbTugasKelas;
    @FXML private DatePicker dpTugasDeadline;
    @FXML private TextField txtSearchTugas;
    @FXML private TableView<Tugas> tblTugas;
    @FXML private TableColumn<Tugas, Integer> colTugasId;
    @FXML private TableColumn<Tugas, String> colTugasJudul;
    @FXML private TableColumn<Tugas, String> colTugasKelas;
    @FXML private TableColumn<Tugas, String> colTugasDeadline;
    private final ObservableList<Tugas> tugasList = FXCollections.observableArrayList();
    private int selectedTugasId = -1;


    // === MATERI ===
    @FXML private TextField txtMateriJudul;
    @FXML private TextArea txtMateriIsi;
    @FXML private ComboBox<String> cmbMateriKelas;
    @FXML private TableView<Materi> tblMateri;
    @FXML private TableColumn<Materi, Integer> colMateriId;
    @FXML private TableColumn<Materi, String> colMateriJudul;
    @FXML private TableColumn<Materi, String> colMateriKelas;
    @FXML private TableColumn<Materi, String> colMateriIsi;
    private final ObservableList<Materi> materiList = FXCollections.observableArrayList();
    private int selectedMateriId = -1;


    // === SUBMISSION ===
    @FXML private TextField txtSearchSubmission;
    @FXML private TableView<Submission> tblSubmission;
    @FXML private TableColumn<Submission, Integer> colSubId;
    @FXML private TableColumn<Submission, String> colSubNamaMhs;
    @FXML private TableColumn<Submission, String> colSubTugas;
    @FXML private TableColumn<Submission, String> colSubKelas;
    @FXML private TableColumn<Submission, String> colSubTanggal;
    @FXML private TableColumn<Submission, String> colSubFile;
    @FXML private TableColumn<Submission, String> colSubNilai;
    private final ObservableList<Submission> submissionList = FXCollections.observableArrayList();


    // === PENGUMUMAN ===
    @FXML private TextField txtPengumumanJudul;
    @FXML private TextArea txtPengumumanIsi;
    @FXML private ComboBox<String> cmbPengumumanKelas;
    @FXML private TableView<Pengumuman> tblPengumuman;
    @FXML private TableColumn<Pengumuman, Integer> colPengId;
    @FXML private TableColumn<Pengumuman, String> colPengJudul;
    @FXML private TableColumn<Pengumuman, String> colPengIsi;
    @FXML private TableColumn<Pengumuman, String> colPengKelas;
    @FXML private TableColumn<Pengumuman, String> colPengTgl;
    private final ObservableList<Pengumuman> pengumumanList = FXCollections.observableArrayList();
    private int selectedPengId = -1;


    // Warna accent per card kelas (cycling)
    private static final String[] CARD_COLORS = {
            "#4F46E5", "#2563EB", "#0891B2", "#059669", "#D97706", "#DC2626", "#7C3AED", "#DB2777"
    };


    @FXML
    public void initialize() {
        lblSidebarNama.setText(Session.getNama() != null ? Session.getNama() : "Dosen");
        lblSidebarEmail.setText(Session.getEmail() != null ? Session.getEmail() : "");


        setupTugasTable();
        setupMateriTable();
        setupSubmissionTable();
        setupPengumumanTable();


        showView("dashboard");
        loadDashboard();
        loadKelasToAllCombos();
    }


    // ===================== NAV =====================


    @FXML private void handleNavDashboard() { showView("dashboard"); loadDashboard(); setActive(btnNavDashboard); }
    @FXML private void handleNavTugas()     { showView("tugas"); loadTugas(); setActive(btnNavTugas); }
    @FXML private void handleNavMateri()    { showView("materi"); loadMateri(); setActive(btnNavMateri); }
    @FXML private void handleNavSubmission(){ showView("submission"); loadSubmission(); setActive(btnNavSubmission); }
    @FXML private void handleNavPengumuman(){ showView("pengumuman"); loadPengumuman(); setActive(btnNavPengumuman); }


    @FXML private void handleLogout() {
        Session.clearSession();
        moveTo("login.fxml");
    }


    private void showView(String v) {
        viewDashboard.setVisible(false); viewTugas.setVisible(false);
        viewMateri.setVisible(false); viewSubmission.setVisible(false); viewPengumuman.setVisible(false);
        switch (v) {
            case "dashboard"   -> viewDashboard.setVisible(true);
            case "tugas"       -> viewTugas.setVisible(true);
            case "materi"      -> viewMateri.setVisible(true);
            case "submission"  -> viewSubmission.setVisible(true);
            case "pengumuman"  -> viewPengumuman.setVisible(true);
        }
    }


    private void setActive(Button b) {
        String off = "-fx-background-color:transparent; -fx-text-fill:#CBD5E1; -fx-font-size:14; -fx-alignment:CENTER_LEFT; -fx-cursor:hand; -fx-background-radius:12;";
        String on  = "-fx-background-color:linear-gradient(to right,#4F46E5,#2563EB); -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:12; -fx-alignment:CENTER_LEFT; -fx-cursor:hand;";
        for (Button btn : new Button[]{btnNavDashboard, btnNavTugas, btnNavMateri, btnNavSubmission, btnNavPengumuman})
            btn.setStyle(off);
        b.setStyle(on);
    }


    // ===================== SETUP TABLES =====================


    private void setupTugasTable() {
        colTugasId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTugasJudul.setCellValueFactory(new PropertyValueFactory<>("judul"));
        colTugasKelas.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        colTugasDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        tblTugas.setItems(tugasList);
        tblTugas.setOnMouseClicked(e -> selectTugas());
        txtSearchTugas.textProperty().addListener((obs, o, n) -> searchTugas(n));
    }


    private void setupMateriTable() {
        colMateriId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMateriJudul.setCellValueFactory(new PropertyValueFactory<>("judul"));
        colMateriKelas.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        colMateriIsi.setCellValueFactory(new PropertyValueFactory<>("isi"));
        tblMateri.setItems(materiList);
        tblMateri.setOnMouseClicked(e -> selectMateri());
    }


    private void setupSubmissionTable() {
        colSubId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSubNamaMhs.setCellValueFactory(new PropertyValueFactory<>("namaMahasiswa"));
        colSubTugas.setCellValueFactory(new PropertyValueFactory<>("namaAssignment"));
        colSubKelas.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        colSubTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalKumpul"));
        colSubFile.setCellValueFactory(new PropertyValueFactory<>("fileUrl"));
        colSubNilai.setCellValueFactory(new PropertyValueFactory<>("nilai"));
        tblSubmission.setItems(submissionList);
        txtSearchSubmission.textProperty().addListener((obs, o, n) -> filterSubmission(n));
    }


    private void setupPengumumanTable() {
        colPengId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPengJudul.setCellValueFactory(new PropertyValueFactory<>("judul"));
        colPengIsi.setCellValueFactory(new PropertyValueFactory<>("isi"));
        colPengKelas.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        colPengTgl.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        tblPengumuman.setItems(pengumumanList);
        tblPengumuman.setOnMouseClicked(e -> selectPengumuman());
    }


    // ===================== LOAD DATA =====================


    private void loadDashboard() {
        // Stats
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            // Total kelas milik dosen ini
            int userId = Session.getUserId();
            PreparedStatement psKelas = conn.prepareStatement(
                    "SELECT COUNT(*) FROM kelas WHERE dosen_id = ?");
            psKelas.setInt(1, userId);
            ResultSet rs = psKelas.executeQuery();
            if (rs.next()) lblTotalAssignment.setText(String.valueOf(rs.getInt(1)));


            // Total submission
            rs = st.executeQuery("SELECT COUNT(*) FROM submission");
            if (rs.next()) lblTotalSubmission.setText(String.valueOf(rs.getInt(1)));


            try {
                rs = st.executeQuery("SELECT COUNT(*) FROM materi");
                if (rs.next()) lblTotalMateri.setText(String.valueOf(rs.getInt(1)));
            } catch (Exception ignored) { lblTotalMateri.setText("0"); }
            try {
                rs = st.executeQuery("SELECT COUNT(*) FROM pengumuman");
                if (rs.next()) lblTotalPengumuman.setText(String.valueOf(rs.getInt(1)));
            } catch (Exception ignored) { lblTotalPengumuman.setText("0"); }


        } catch (Exception e) { e.printStackTrace(); }


        // Load kelas cards
        loadKelasCards();
    }


    private void loadKelasCards() {
        flowKelas.getChildren().clear();
        int userId = Session.getUserId();


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, nama FROM kelas WHERE dosen_id = ? ORDER BY nama")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();


            int colorIdx = 0;
            while (rs.next()) {
                String kelasId = rs.getString("id");
                String kelasNama = rs.getString("nama");
                String color = CARD_COLORS[colorIdx % CARD_COLORS.length];
                colorIdx++;


                VBox card = buildKelasCard(kelasId, kelasNama, color);
                flowKelas.getChildren().add(card);
            }


            if (flowKelas.getChildren().isEmpty()) {
                Label empty = new Label("Anda belum mengampu kelas manapun.");
                empty.setStyle("-fx-text-fill:#94A3B8; -fx-font-size:14;");
                flowKelas.getChildren().add(empty);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Membuat card kelas mirip Moodle: banner warna di atas, nama kelas, tombol Buka.
     */
    private VBox buildKelasCard(String kelasId, String kelasNama, String accentColor) {
        // Banner warna atas
        Pane banner = new Pane();
        banner.setPrefHeight(80);
        banner.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 14 14 0 0;");


        // Ikon kelas di banner
        Label icon = new Label("🎓");
        icon.setStyle("-fx-font-size:28;");
        icon.setLayoutX(12);
        icon.setLayoutY(24);
        banner.getChildren().add(icon);


        // Body card
        VBox body = new VBox(8);
        body.setPadding(new Insets(14, 16, 14, 16));
        body.setStyle("-fx-background-color:white; -fx-background-radius: 0 0 14 14;");


        Label lblNama = new Label(kelasNama);
        lblNama.setStyle("-fx-font-size:14; -fx-font-weight:bold; -fx-text-fill:#0F172A;");
        lblNama.setWrapText(true);
        lblNama.setMaxWidth(200);


        Label lblId = new Label("ID: " + kelasId);
        lblId.setStyle("-fx-font-size:11; -fx-text-fill:#94A3B8;");


        Button btnBuka = new Button("Buka Kelas →");
        btnBuka.setMaxWidth(Double.MAX_VALUE);
        btnBuka.setPrefHeight(34);
        btnBuka.setStyle("-fx-background-color:" + accentColor + "; -fx-text-fill:white; " +
                "-fx-background-radius:8; -fx-font-size:12; -fx-font-weight:bold; -fx-cursor:hand;");
        btnBuka.setOnAction(e -> openKelas(kelasId, kelasNama));


        body.getChildren().addAll(lblNama, lblId, btnBuka);


        // Gabung banner + body
        VBox card = new VBox();
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        card.setStyle("-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),10,0,0,3); -fx-background-radius:14;");
        card.getChildren().addAll(banner, body);


        return card;
    }


    /**
     * Pindah ke halaman per kelas (dosen_kelas.fxml), pass kelasId & kelasNama via Session.
     */
    private void openKelas(String kelasId, String kelasNama) {
        Session.setCurrentKelasId(kelasId);
        Session.setCurrentKelasNama(kelasNama);
        moveTo("dosen_kelas.fxml");
    }


    private void loadTugas() {
        tugasList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT a.id, a.judul, k.nama AS kelas, a.deadline FROM assignment a " +
                             "LEFT JOIN kelas k ON a.kelas_id = k.id ORDER BY a.id")) {
            while (rs.next())
                tugasList.add(new Tugas(rs.getInt("id"), rs.getString("judul"),
                        nvl(rs.getString("kelas")), nvl(rs.getString("deadline"))));
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


    private void loadSubmission() {
        submissionList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT s.id, u.nama AS nama_mhs, a.judul AS nama_tugas, " +
                             "k.nama AS kelas, s.tanggal_submit, s.file_url, s.nilai " +
                             "FROM submission s " +
                             "LEFT JOIN users u ON s.user_id = u.id " +
                             "LEFT JOIN assignment a ON s.assignment_id = a.id " +
                             "LEFT JOIN kelas k ON a.kelas_id = k.id " +
                             "ORDER BY s.tanggal_submit DESC")) {
            while (rs.next())
                submissionList.add(new Submission(rs.getInt("id"),
                        nvl(rs.getString("nama_mhs")),
                        nvl(rs.getString("nama_tugas")),
                        nvl(rs.getString("kelas")),
                        nvl(rs.getString("tanggal_submit")),
                        nvl(rs.getString("file_url")),
                        nvl(rs.getString("nilai"))));
        } catch (Exception e) { e.printStackTrace(); }
    }


    private void loadPengumuman() {
        pengumumanList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT p.id, p.judul, p.isi, k.nama AS kelas, p.tanggal FROM pengumuman p " +
                             "LEFT JOIN kelas k ON p.kelas_id = k.id ORDER BY p.id DESC")) {
            while (rs.next())
                pengumumanList.add(new Pengumuman(rs.getInt("id"), rs.getString("judul"),
                        nvl(rs.getString("isi")), nvl(rs.getString("kelas")), nvl(rs.getString("tanggal"))));
        } catch (Exception e) { e.printStackTrace(); }
    }


    private void loadKelasToAllCombos() {
        ObservableList<String> kelasList = FXCollections.observableArrayList();
        int userId = Session.getUserId();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT nama FROM kelas WHERE dosen_id = ? ORDER BY nama")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) kelasList.add(rs.getString("nama"));
        } catch (Exception e) { e.printStackTrace(); }
        cmbTugasKelas.setItems(kelasList);
        cmbMateriKelas.setItems(FXCollections.observableArrayList(kelasList));
        cmbPengumumanKelas.setItems(FXCollections.observableArrayList(kelasList));
    }


    // ===================== TUGAS HANDLERS =====================


    @FXML private void handleTambahTugas() {
        String judul = txtTugasJudul.getText().trim();
        String kelas = cmbTugasKelas.getValue();
        LocalDate deadline = dpTugasDeadline.getValue();
        if (judul.isEmpty() || kelas == null || deadline == null) {
            showAlert("Gagal", "Judul, kelas, dan deadline wajib diisi."); return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            String kelasId = getKelasId(conn, kelas);
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO assignment(judul, kelas_id, deadline) VALUES (?,?,?)");
            ps.setString(1, judul);
            ps.setString(2, kelasId);
            ps.setDate(3, java.sql.Date.valueOf(deadline));
            ps.executeUpdate();
            showInfo("Berhasil", "Tugas ditambahkan.");
            loadTugas(); handleClearTugas();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
    }


    @FXML private void handleEditTugas() {
        if (selectedTugasId == -1) { showAlert("Pilih Data", "Pilih tugas dulu."); return; }
        String judul = txtTugasJudul.getText().trim();
        String kelas = cmbTugasKelas.getValue();
        LocalDate deadline = dpTugasDeadline.getValue();
        if (judul.isEmpty()) { showAlert("Gagal", "Judul wajib diisi."); return; }
        try (Connection conn = DatabaseConnection.getConnection()) {
            String kelasId = kelas != null ? getKelasId(conn, kelas) : null;
            String sql = (kelasId != null && !kelasId.isEmpty())
                    ? "UPDATE assignment SET judul=?, kelas_id=?, deadline=? WHERE id=?"
                    : "UPDATE assignment SET judul=?, deadline=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            if (kelasId != null && !kelasId.isEmpty()) {
                ps.setString(1, judul);
                ps.setString(2, kelasId);
                if (deadline != null) ps.setDate(3, java.sql.Date.valueOf(deadline));
                else ps.setNull(3, java.sql.Types.DATE);
                ps.setInt(4, selectedTugasId);
            } else {
                ps.setString(1, judul);
                if (deadline != null) ps.setDate(2, java.sql.Date.valueOf(deadline));
                else ps.setNull(2, java.sql.Types.DATE);
                ps.setInt(3, selectedTugasId);
            }
            ps.executeUpdate();
            showInfo("Berhasil", "Tugas diupdate."); loadTugas(); handleClearTugas();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
    }


    @FXML private void handleHapusTugas() {
        if (selectedTugasId == -1) { showAlert("Pilih Data", "Pilih tugas dulu."); return; }
        Optional<ButtonType> r = confirm("Yakin hapus tugas ini?");
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM assignment WHERE id=?")) {
                ps.setInt(1, selectedTugasId);
                ps.executeUpdate();
                showInfo("Berhasil", "Tugas dihapus."); loadTugas(); handleClearTugas();
            } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
        }
    }


    @FXML private void handleClearTugas() {
        txtTugasJudul.clear(); txtTugasDeskripsi.clear();
        cmbTugasKelas.setValue(null); dpTugasDeadline.setValue(null);
        selectedTugasId = -1; tblTugas.getSelectionModel().clearSelection();
    }


    private void selectTugas() {
        Tugas t = tblTugas.getSelectionModel().getSelectedItem();
        if (t != null) {
            selectedTugasId = t.getId();
            txtTugasJudul.setText(t.getJudul());
            cmbTugasKelas.setValue(t.getKelas());
            try { dpTugasDeadline.setValue(LocalDate.parse(t.getDeadline())); } catch (Exception ignored) {}
        }
    }


    private void searchTugas(String kw) {
        tugasList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT a.id, a.judul, k.nama AS kelas, a.deadline FROM assignment a " +
                             "LEFT JOIN kelas k ON a.kelas_id = k.id " +
                             "WHERE LOWER(a.judul) LIKE ? ORDER BY a.id")) {
            ps.setString(1, "%" + kw.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                tugasList.add(new Tugas(rs.getInt("id"), rs.getString("judul"),
                        nvl(rs.getString("kelas")), nvl(rs.getString("deadline"))));
        } catch (Exception e) { e.printStackTrace(); }
    }


    // ===================== MATERI HANDLERS =====================


    @FXML private void handleTambahMateri() {
        String judul = txtMateriJudul.getText().trim();
        String isi = txtMateriIsi.getText().trim();
        String kelas = cmbMateriKelas.getValue();
        if (judul.isEmpty() || isi.isEmpty()) { showAlert("Gagal", "Judul dan isi wajib diisi."); return; }
        try (Connection conn = DatabaseConnection.getConnection()) {
            String kelasId = kelas != null ? getKelasId(conn, kelas) : null;
            PreparedStatement ps = (kelasId != null && !kelasId.isEmpty())
                    ? conn.prepareStatement("INSERT INTO materi(judul, isi, kelas_id) VALUES (?,?,?)")
                    : conn.prepareStatement("INSERT INTO materi(judul, isi) VALUES (?,?)");
            ps.setString(1, judul); ps.setString(2, isi);
            if (kelasId != null && !kelasId.isEmpty()) ps.setString(3, kelasId);
            ps.executeUpdate();
            showInfo("Berhasil", "Materi ditambahkan."); loadMateri(); handleClearMateri();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
    }


    @FXML private void handleHapusMateri() {
        if (selectedMateriId == -1) { showAlert("Pilih Data", "Pilih materi dulu."); return; }
        Optional<ButtonType> r = confirm("Yakin hapus materi ini?");
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM materi WHERE id=?")) {
                ps.setInt(1, selectedMateriId);
                ps.executeUpdate();
                showInfo("Berhasil", "Materi dihapus."); loadMateri(); handleClearMateri();
            } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
        }
    }


    @FXML private void handleClearMateri() {
        txtMateriJudul.clear(); txtMateriIsi.clear(); cmbMateriKelas.setValue(null);
        selectedMateriId = -1; tblMateri.getSelectionModel().clearSelection();
    }


    private void selectMateri() {
        Materi m = tblMateri.getSelectionModel().getSelectedItem();
        if (m != null) {
            selectedMateriId = m.getId();
            txtMateriJudul.setText(m.getJudul());
            txtMateriIsi.setText(m.getIsi());
            cmbMateriKelas.setValue(m.getKelas());
        }
    }


    // ===================== SUBMISSION HANDLERS =====================


    @FXML private void handleBeriNilai() {
        Submission s = tblSubmission.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("Pilih Data", "Pilih submission dulu."); return; }
        TextInputDialog dlg = new TextInputDialog(s.getNilai());
        dlg.setTitle("Beri Nilai");
        dlg.setHeaderText("Submission: " + s.getNamaAssignment() + " - " + s.getNamaMahasiswa());
        dlg.setContentText("Nilai (0-100):");
        dlg.showAndWait().ifPresent(nilai -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE submission SET nilai=? WHERE id=?")) {
                ps.setString(1, nilai.trim());
                ps.setInt(2, s.getId());
                ps.executeUpdate();
                showInfo("Berhasil", "Nilai disimpan."); loadSubmission();
            } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
        });
    }


    private void filterSubmission(String kw) {
        if (kw == null || kw.isEmpty()) { tblSubmission.setItems(submissionList); return; }
        ObservableList<Submission> filtered = FXCollections.observableArrayList();
        for (Submission s : submissionList)
            if (s.getNamaMahasiswa().toLowerCase().contains(kw.toLowerCase()) ||
                    s.getNamaAssignment().toLowerCase().contains(kw.toLowerCase()))
                filtered.add(s);
        tblSubmission.setItems(filtered);
    }


    // ===================== PENGUMUMAN HANDLERS =====================


    @FXML private void handleKirimPengumuman() {
        String judul = txtPengumumanJudul.getText().trim();
        String isi = txtPengumumanIsi.getText().trim();
        String kelas = cmbPengumumanKelas.getValue();
        if (judul.isEmpty() || isi.isEmpty()) { showAlert("Gagal", "Judul dan isi wajib diisi."); return; }
        try (Connection conn = DatabaseConnection.getConnection()) {
            String kelasId = kelas != null ? getKelasId(conn, kelas) : null;
            PreparedStatement ps;
            if (kelasId != null && !kelasId.isEmpty()) {
                ps = conn.prepareStatement("INSERT INTO pengumuman(judul, isi, kelas_id, tanggal) VALUES (?,?,?,CURRENT_DATE)");
                ps.setString(1, judul); ps.setString(2, isi); ps.setString(3, kelasId);
            } else {
                ps = conn.prepareStatement("INSERT INTO pengumuman(judul, isi, tanggal) VALUES (?,?,CURRENT_DATE)");
                ps.setString(1, judul); ps.setString(2, isi);
            }
            ps.executeUpdate();
            showInfo("Berhasil", "Pengumuman dikirim."); loadPengumuman(); handleClearPengumuman();
        } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
    }


    @FXML private void handleHapusPengumuman() {
        if (selectedPengId == -1) { showAlert("Pilih Data", "Pilih pengumuman dulu."); return; }
        Optional<ButtonType> r = confirm("Yakin hapus pengumuman ini?");
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM pengumuman WHERE id=?")) {
                ps.setInt(1, selectedPengId);
                ps.executeUpdate();
                showInfo("Berhasil", "Pengumuman dihapus."); loadPengumuman(); handleClearPengumuman();
            } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
        }
    }


    @FXML private void handleClearPengumuman() {
        txtPengumumanJudul.clear(); txtPengumumanIsi.clear(); cmbPengumumanKelas.setValue(null);
        selectedPengId = -1; tblPengumuman.getSelectionModel().clearSelection();
    }


    private void selectPengumuman() {
        Pengumuman p = tblPengumuman.getSelectionModel().getSelectedItem();
        if (p != null) {
            selectedPengId = p.getId();
            txtPengumumanJudul.setText(p.getJudul());
            txtPengumumanIsi.setText(p.getIsi());
        }
    }


    // ===================== UTILS =====================


    private String getKelasId(Connection conn, String namaKelas) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT id FROM kelas WHERE nama=?");
        ps.setString(1, namaKelas);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getString("id") : null;
    }


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
    private Optional<ButtonType> confirm(String m) {
        return new Alert(Alert.AlertType.CONFIRMATION, m, ButtonType.OK, ButtonType.CANCEL) {{ setHeaderText(null); }}.showAndWait();
    }


    // ===================== MODELS =====================


    public static class Tugas {
        private final int id; private final String judul, kelas, deadline;
        public Tugas(int id, String judul, String kelas, String deadline) { this.id=id; this.judul=judul; this.kelas=kelas; this.deadline=deadline; }
        public int getId() { return id; } public String getJudul() { return judul; }
        public String getKelas() { return kelas; } public String getDeadline() { return deadline; }
    }


    public static class Materi {
        private final int id; private final String judul, kelas, isi;
        public Materi(int id, String judul, String kelas, String isi) { this.id=id; this.judul=judul; this.kelas=kelas; this.isi=isi; }
        public int getId() { return id; } public String getJudul() { return judul; }
        public String getKelas() { return kelas; } public String getIsi() { return isi; }
    }


    public static class Submission {
        private final int id; private final String namaMahasiswa, namaAssignment, kelas, tanggalKumpul, fileUrl, nilai;
        public Submission(int id, String namaMahasiswa, String namaAssignment, String kelas, String tanggalKumpul, String fileUrl, String nilai) {
            this.id=id; this.namaMahasiswa=namaMahasiswa; this.namaAssignment=namaAssignment;
            this.kelas=kelas; this.tanggalKumpul=tanggalKumpul; this.fileUrl=fileUrl; this.nilai=nilai;
        }
        public int getId() { return id; } public String getNamaMahasiswa() { return namaMahasiswa; }
        public String getNamaAssignment() { return namaAssignment; } public String getKelas() { return kelas; }
        public String getTanggalKumpul() { return tanggalKumpul; } public String getFileUrl() { return fileUrl; }
        public String getNilai() { return nilai; }
    }


    public static class Pengumuman {
        private final int id; private final String judul, isi, kelas, tanggal;
        public Pengumuman(int id, String judul, String isi, String kelas, String tanggal) {
            this.id=id; this.judul=judul; this.isi=isi; this.kelas=kelas; this.tanggal=tanggal;
        }
        public int getId() { return id; } public String getJudul() { return judul; }
        public String getIsi() { return isi; } public String getKelas() { return kelas; }
        public String getTanggal() { return tanggal; }
    }
}

