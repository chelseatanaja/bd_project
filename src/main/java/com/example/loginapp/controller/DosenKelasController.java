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


import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;


public class DosenKelasController {


    // === SIDEBAR ===
    @FXML private Button btnNavMateri;
    @FXML private Button btnNavTugas;
    @FXML private Button btnNavSubmission;
    @FXML private Label lblSidebarNama;
    @FXML private Label lblSidebarEmail;
    @FXML private Label lblNamaKelas;
    @FXML private Label lblIdKelas;


    // === VIEWS ===
    @FXML private VBox viewMateri;
    @FXML private VBox viewTugas;
    @FXML private VBox viewSubmission;


    // === MATERI ===
    @FXML private VBox panelFormMateri;
    @FXML private Label lblFormTitle;
    @FXML private Label lblHeaderKelas;
    @FXML private Label lblJumlahMateri;
    @FXML private TextField txtMateriJudul;
    @FXML private TextField txtMateriIsi;
    @FXML private Button btnSimpanMateri;
    @FXML private TableView<MateriItem> tblMateri;
    @FXML private TableColumn<MateriItem, Integer> colMateriId;
    @FXML private TableColumn<MateriItem, String> colMateriJudul;
    @FXML private TableColumn<MateriItem, String> colMateriIsi;
    private final ObservableList<MateriItem> materiList = FXCollections.observableArrayList();
    private int selectedMateriId = -1;
    private boolean isEditMode = false;


    // === TUGAS ===
    @FXML private Label lblHeaderTugas;
    @FXML private VBox panelFormTugas;
    @FXML private Label lblFormTugasTitle;
    @FXML private Label lblJumlahTugas;
    @FXML private TextField txtTugasJudul;
    @FXML private DatePicker dpTugasDeadline;
    @FXML private Button btnSimpanTugas;
    @FXML private TableView<TugasItem> tblTugas;
    @FXML private TableColumn<TugasItem, Integer> colTugasId;
    @FXML private TableColumn<TugasItem, String> colTugasJudul;
    @FXML private TableColumn<TugasItem, String> colTugasDeadline;
    private final ObservableList<TugasItem> tugasList = FXCollections.observableArrayList();
    private int selectedTugasId = -1;
    private boolean isEditTugasMode = false;


    // === SUBMISSION ===
    @FXML private TableView<SubmissionItem> tblSubmission;
    @FXML private TableColumn<SubmissionItem, Integer> colSubId;
    @FXML private TableColumn<SubmissionItem, String> colSubNamaMhs;
    @FXML private TableColumn<SubmissionItem, String> colSubTugas;
    @FXML private TableColumn<SubmissionItem, String> colSubTanggal;
    @FXML private TableColumn<SubmissionItem, String> colSubFile;
    @FXML private TableColumn<SubmissionItem, String> colSubNilai;
    private final ObservableList<SubmissionItem> submissionList = FXCollections.observableArrayList();


    // Kelas aktif (diambil dari Session)
    private String currentKelasId;
    private String currentKelasNama;


    @FXML
    public void initialize() {
        // Ambil data kelas dari Session
        currentKelasId   = Session.getCurrentKelasId();
        currentKelasNama = Session.getCurrentKelasNama();


        // Sidebar info
        lblSidebarNama.setText(Session.getNama() != null ? Session.getNama() : "Dosen");
        lblSidebarEmail.setText(Session.getEmail() != null ? Session.getEmail() : "");
        lblNamaKelas.setText(currentKelasNama != null ? currentKelasNama : "-");
        lblIdKelas.setText("ID: " + (currentKelasId != null ? currentKelasId : "-"));


        // Header
        lblHeaderKelas.setText("Materi — " + (currentKelasNama != null ? currentKelasNama : ""));
        lblHeaderTugas.setText("Tugas — " + (currentKelasNama != null ? currentKelasNama : ""));


        setupMateriTable();
        setupTugasTable();
        setupSubmissionTable();


        showView("materi");
        loadMateri();
    }


    // ===================== NAV =====================


    @FXML private void handleNavMateri()    { showView("materi"); loadMateri(); setActive(btnNavMateri); }
    @FXML private void handleNavTugas()     { showView("tugas"); loadTugas(); setActive(btnNavTugas); }
    @FXML private void handleNavSubmission(){ showView("submission"); loadSubmission(); setActive(btnNavSubmission); }


    @FXML private void handleBackToDashboard() {
        moveTo("dosen_dashboard.fxml");
    }


    @FXML private void handleLogout() {
        Session.clearSession();
        moveTo("login.fxml");
    }


    private void showView(String v) {
        viewMateri.setVisible(false);
        viewTugas.setVisible(false);
        viewSubmission.setVisible(false);
        switch (v) {
            case "materi"     -> viewMateri.setVisible(true);
            case "tugas"      -> viewTugas.setVisible(true);
            case "submission" -> viewSubmission.setVisible(true);
        }
    }


    private void setActive(Button b) {
        String off = "-fx-background-color:transparent; -fx-text-fill:#CBD5E1; -fx-font-size:14; -fx-alignment:CENTER_LEFT; -fx-cursor:hand; -fx-background-radius:12;";
        String on  = "-fx-background-color:linear-gradient(to right,#E4568B,#E4568B); -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:12; -fx-alignment:CENTER_LEFT; -fx-cursor:hand;";
        for (Button btn : new Button[]{btnNavMateri, btnNavTugas, btnNavSubmission})
            btn.setStyle(off);
        b.setStyle(on);
    }


    // ===================== SETUP TABLES =====================


    private void setupMateriTable() {
        colMateriId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMateriJudul.setCellValueFactory(new PropertyValueFactory<>("judul"));
        colMateriIsi.setCellValueFactory(new PropertyValueFactory<>("isi"));
        tblMateri.setItems(materiList);
        tblMateri.setOnMouseClicked(e -> {
            MateriItem m = tblMateri.getSelectionModel().getSelectedItem();
            if (m != null) selectedMateriId = m.getId();
        });
    }


    private void setupTugasTable() {
        colTugasId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTugasJudul.setCellValueFactory(new PropertyValueFactory<>("judul"));
        colTugasDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        tblTugas.setItems(tugasList);
        tblTugas.setOnMouseClicked(e -> {
            TugasItem t = tblTugas.getSelectionModel().getSelectedItem();
            if (t != null) selectedTugasId = t.getId();
        });
    }


    private void setupSubmissionTable() {
        colSubId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSubNamaMhs.setCellValueFactory(new PropertyValueFactory<>("namaMahasiswa"));
        colSubTugas.setCellValueFactory(new PropertyValueFactory<>("namaAssignment"));
        colSubTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalKumpul"));
        colSubFile.setCellValueFactory(new PropertyValueFactory<>("fileUrl"));
        colSubNilai.setCellValueFactory(new PropertyValueFactory<>("nilai"));
        tblSubmission.setItems(submissionList);
    }


    // ===================== LOAD DATA =====================


    private void loadMateri() {
        materiList.clear();
        if (currentKelasId == null) return;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, judul, isi FROM materi WHERE kelas_id = ? ORDER BY id")) {
            ps.setString(1, currentKelasId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                materiList.add(new MateriItem(rs.getInt("id"),
                        nvl(rs.getString("judul")), nvl(rs.getString("isi"))));
        } catch (Exception e) { e.printStackTrace(); }
        lblJumlahMateri.setText(materiList.size() + " materi");
    }


    private void loadTugas() {
        tugasList.clear();
        if (currentKelasId == null) return;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, judul, deadline FROM assignment WHERE kelas_id = ? ORDER BY deadline")) {
            ps.setString(1, currentKelasId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                tugasList.add(new TugasItem(rs.getInt("id"),
                        nvl(rs.getString("judul")), nvl(rs.getString("deadline"))));
        } catch (Exception e) { e.printStackTrace(); }
        lblJumlahTugas.setText(tugasList.size() + " tugas");
    }


    private void loadSubmission() {
        submissionList.clear();
        if (currentKelasId == null) return;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT s.id, u.nama AS nama_mhs, a.judul AS nama_tugas, " +
                             "s.tanggal_submit, s.file_url, s.nilai " +
                             "FROM submission s " +
                             "LEFT JOIN users u ON s.user_id = u.id " +
                             "LEFT JOIN assignment a ON s.assignment_id = a.id " +
                             "WHERE a.kelas_id = ? ORDER BY s.tanggal_submit DESC")) {
            ps.setString(1, currentKelasId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                submissionList.add(new SubmissionItem(rs.getInt("id"),
                        nvl(rs.getString("nama_mhs")),
                        nvl(rs.getString("nama_tugas")),
                        nvl(rs.getString("tanggal_submit")),
                        nvl(rs.getString("file_url")),
                        nvl(rs.getString("nilai"))));
        } catch (Exception e) { e.printStackTrace(); }
    }


    // ===================== MATERI FORM HANDLERS =====================


    @FXML private void handleShowFormMateri() {
        isEditMode = false;
        selectedMateriId = -1;
        lblFormTitle.setText("Tambah Materi Baru");
        btnSimpanMateri.setText("Simpan");
        txtMateriJudul.clear();
        txtMateriIsi.clear();
        tblMateri.getSelectionModel().clearSelection();
        panelFormMateri.setVisible(true);
        panelFormMateri.setManaged(true);
    }


    @FXML private void handleHideFormMateri() {
        panelFormMateri.setVisible(false);
        panelFormMateri.setManaged(false);
        txtMateriJudul.clear();
        txtMateriIsi.clear();
        selectedMateriId = -1;
        isEditMode = false;
        tblMateri.getSelectionModel().clearSelection();
    }


    @FXML private void handleEditMateri() {
        MateriItem m = tblMateri.getSelectionModel().getSelectedItem();
        if (m == null) { showAlert("Pilih Data", "Pilih materi dulu dari tabel."); return; }
        isEditMode = true;
        selectedMateriId = m.getId();
        lblFormTitle.setText("Edit Materi");
        btnSimpanMateri.setText("Update");
        txtMateriJudul.setText(m.getJudul());
        txtMateriIsi.setText(m.getIsi());
        panelFormMateri.setVisible(true);
        panelFormMateri.setManaged(true);
    }


    @FXML private void handleSimpanMateri() {
        String judul = txtMateriJudul.getText().trim();
        String isi   = txtMateriIsi.getText().trim();
        if (judul.isEmpty()) { showAlert("Gagal", "Judul materi wajib diisi."); return; }


        try (Connection conn = DatabaseConnection.getConnection()) {
            if (!isEditMode) {
                // INSERT
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO materi(judul, isi, kelas_id) VALUES (?,?,?)");
                ps.setString(1, judul);
                ps.setString(2, isi.isEmpty() ? null : isi);
                ps.setString(3, currentKelasId);
                ps.executeUpdate();
                showInfo("Berhasil", "Materi berhasil ditambahkan.");
            } else {
                // UPDATE
                if (selectedMateriId == -1) { showAlert("Error", "Tidak ada materi yang dipilih."); return; }
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE materi SET judul=?, isi=? WHERE id=? AND kelas_id=?");
                ps.setString(1, judul);
                ps.setString(2, isi.isEmpty() ? null : isi);
                ps.setInt(3, selectedMateriId);
                ps.setString(4, currentKelasId);
                ps.executeUpdate();
                showInfo("Berhasil", "Materi berhasil diupdate.");
            }
            handleHideFormMateri();
            loadMateri();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", e.getMessage());
        }
    }


    @FXML private void handleHapusMateri() {
        MateriItem m = tblMateri.getSelectionModel().getSelectedItem();
        if (m == null) { showAlert("Pilih Data", "Pilih materi dulu dari tabel."); return; }
        Optional<ButtonType> r = confirm("Yakin hapus materi \"" + m.getJudul() + "\"?");
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM materi WHERE id=?")) {
                ps.setInt(1, m.getId());
                ps.executeUpdate();
                showInfo("Berhasil", "Materi dihapus.");
                handleHideFormMateri();
                loadMateri();
            } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
        }
    }


    // ===================== TUGAS FORM HANDLERS =====================


    @FXML private void handleShowFormTugas() {
        isEditTugasMode = false;
        selectedTugasId = -1;
        lblFormTugasTitle.setText("Tambah Tugas Baru");
        btnSimpanTugas.setText("Simpan");
        txtTugasJudul.clear();
        dpTugasDeadline.setValue(null);
        tblTugas.getSelectionModel().clearSelection();
        panelFormTugas.setVisible(true);
        panelFormTugas.setManaged(true);
    }


    @FXML private void handleHideFormTugas() {
        panelFormTugas.setVisible(false);
        panelFormTugas.setManaged(false);
        txtTugasJudul.clear();
        dpTugasDeadline.setValue(null);
        selectedTugasId = -1;
        isEditTugasMode = false;
        tblTugas.getSelectionModel().clearSelection();
    }


    @FXML private void handleEditTugas() {
        TugasItem t = tblTugas.getSelectionModel().getSelectedItem();
        if (t == null) { showAlert("Pilih Data", "Pilih tugas dulu dari tabel."); return; }
        isEditTugasMode = true;
        selectedTugasId = t.getId();
        lblFormTugasTitle.setText("Edit Tugas");
        btnSimpanTugas.setText("Update");
        txtTugasJudul.setText(t.getJudul());
        try { dpTugasDeadline.setValue(LocalDate.parse(t.getDeadline())); } catch (Exception ignored) { dpTugasDeadline.setValue(null); }
        panelFormTugas.setVisible(true);
        panelFormTugas.setManaged(true);
    }


    @FXML private void handleSimpanTugas() {
        String judul = txtTugasJudul.getText().trim();
        LocalDate deadline = dpTugasDeadline.getValue();
        if (judul.isEmpty()) { showAlert("Gagal", "Judul tugas wajib diisi."); return; }


        try (Connection conn = DatabaseConnection.getConnection()) {
            if (!isEditTugasMode) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO assignment(judul, kelas_id, deadline) VALUES (?,?,?)");
                ps.setString(1, judul);
                ps.setString(2, currentKelasId);
                if (deadline != null) ps.setDate(3, java.sql.Date.valueOf(deadline));
                else ps.setNull(3, java.sql.Types.DATE);
                ps.executeUpdate();
                showInfo("Berhasil", "Tugas berhasil ditambahkan.");
            } else {
                if (selectedTugasId == -1) { showAlert("Error", "Tidak ada tugas yang dipilih."); return; }
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE assignment SET judul=?, deadline=? WHERE id=? AND kelas_id=?");
                ps.setString(1, judul);
                if (deadline != null) ps.setDate(2, java.sql.Date.valueOf(deadline));
                else ps.setNull(2, java.sql.Types.DATE);
                ps.setInt(3, selectedTugasId);
                ps.setString(4, currentKelasId);
                ps.executeUpdate();
                showInfo("Berhasil", "Tugas berhasil diupdate.");
            }
            handleHideFormTugas();
            loadTugas();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", e.getMessage());
        }
    }


    @FXML private void handleHapusTugas() {
        TugasItem t = tblTugas.getSelectionModel().getSelectedItem();
        if (t == null) { showAlert("Pilih Data", "Pilih tugas dulu dari tabel."); return; }
        Optional<ButtonType> r = confirm("Yakin hapus tugas \"" + t.getJudul() + "\"?");
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM assignment WHERE id=?")) {
                ps.setInt(1, t.getId());
                ps.executeUpdate();
                showInfo("Berhasil", "Tugas dihapus.");
                handleHideFormTugas();
                loadTugas();
            } catch (Exception e) { e.printStackTrace(); showAlert("Error", e.getMessage()); }
        }
    }


    // ===================== SUBMISSION =====================


    @FXML private void handleBeriNilai() {
        SubmissionItem s = tblSubmission.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("Pilih Data", "Pilih submission dulu."); return; }
        TextInputDialog dlg = new TextInputDialog(s.getNilai());
        dlg.setTitle("Beri Nilai");
        dlg.setHeaderText(s.getNamaAssignment() + " — " + s.getNamaMahasiswa());
        dlg.setContentText("Nilai (0-100):");
        dlg.showAndWait().ifPresent(nilai -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE submission SET nilai=? WHERE id=?")) {
                BigDecimal n = new BigDecimal(nilai.trim());
                if (n.compareTo(BigDecimal.ZERO) < 0 ||
                        n.compareTo(new BigDecimal("100")) > 0) {
                    showAlert("Error", "Nilai harus antara 0 dan 100.");
                    return;
                }
                ps.setBigDecimal(1, n);
                ps.setInt(2, s.getId());
                ps.executeUpdate();
                showInfo("Berhasil", "Nilai disimpan.");
                loadSubmission();
            } catch (NumberFormatException e) {
                showAlert("Error", "Masukkan angka yang valid.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", e.getMessage());
            }
        });
    }


    // ===================== UTILS =====================


    private String nvl(String s) { return s != null ? s : "-"; }


    private void moveTo(String fxml) {
        try {
            Stage stage = (Stage) btnNavMateri.getScene().getWindow();
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


    public static class MateriItem {
        private final int id; private final String judul, isi;
        public MateriItem(int id, String judul, String isi) { this.id=id; this.judul=judul; this.isi=isi; }
        public int getId() { return id; }
        public String getJudul() { return judul; }
        public String getIsi() { return isi; }
    }


    public static class TugasItem {
        private final int id; private final String judul, deadline;
        public TugasItem(int id, String judul, String deadline) { this.id=id; this.judul=judul; this.deadline=deadline; }
        public int getId() { return id; }
        public String getJudul() { return judul; }
        public String getDeadline() { return deadline; }
    }


    public static class SubmissionItem {
        private final int id; private final String namaMahasiswa, namaAssignment, tanggalKumpul, fileUrl, nilai;
        public SubmissionItem(int id, String namaMahasiswa, String namaAssignment, String tanggalKumpul, String fileUrl, String nilai) {
            this.id=id; this.namaMahasiswa=namaMahasiswa; this.namaAssignment=namaAssignment;
            this.tanggalKumpul=tanggalKumpul; this.fileUrl=fileUrl; this.nilai=nilai;
        }
        public int getId() { return id; }
        public String getNamaMahasiswa() { return namaMahasiswa; }
        public String getNamaAssignment() { return namaAssignment; }
        public String getTanggalKumpul() { return tanggalKumpul; }
        public String getFileUrl() { return fileUrl; }
        public String getNilai() { return nilai; }
    }
}

