package com.example.loginapp.controller;

import com.example.loginapp.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.Pattern;

public class SignUpController {

    // Regex sederhana tapi cukup ketat buat validasi format email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$"
    );

    @FXML private TextField txtNama;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Button btnSignUp;
    @FXML private Hyperlink btnLogin;

    // Label error inline (di bawah masing-masing field) — perlu ditambahkan di FXML
    @FXML private Label lblNamaError;
    @FXML private Label lblEmailError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblConfirmPasswordError;
    @FXML private Label lblRoleError;

    @FXML
    public void initialize() {
        cmbRole.getItems().addAll("Admin", "Dosen", "Mahasiswa");
        btnSignUp.setOnAction(e -> signUp());
        btnLogin.setOnAction(e -> moveTo("login.fxml"));

        hideAllErrors();
    }

    private void hideAllErrors() {
        lblNamaError.setVisible(false);
        lblEmailError.setVisible(false);
        lblPasswordError.setVisible(false);
        lblConfirmPasswordError.setVisible(false);
        lblRoleError.setVisible(false);
    }

    private void signUp() {
        String nama = txtNama.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();
        String confirmPassword = txtConfirmPassword.getText().trim();
        String role = cmbRole.getValue();

        boolean valid = true;

        // ===== VALIDASI NAMA =====
        if (nama.isEmpty()) {
            setError(lblNamaError, "Nama wajib diisi.");
            valid = false;
        } else {
            lblNamaError.setVisible(false);
        }

        // ===== VALIDASI EMAIL =====
        if (email.isEmpty()) {
            setError(lblEmailError, "Email wajib diisi.");
            valid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            setError(lblEmailError, "Format email tidak valid (contoh: nama@domain.com).");
            valid = false;
        } else {
            lblEmailError.setVisible(false);
        }

        // ===== VALIDASI PASSWORD =====
        if (password.isEmpty()) {
            setError(lblPasswordError, "Password wajib diisi.");
            valid = false;
        } else if (password.length() < 8) {
            setError(lblPasswordError, "Password minimal 8 karakter.");
            valid = false;
        } else {
            lblPasswordError.setVisible(false);
        }

        // ===== VALIDASI KONFIRMASI PASSWORD =====
        if (confirmPassword.isEmpty()) {
            setError(lblConfirmPasswordError, "Konfirmasi password wajib diisi.");
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            setError(lblConfirmPasswordError, "Password tidak sama.");
            valid = false;
        } else {
            lblConfirmPasswordError.setVisible(false);
        }

        // ===== VALIDASI ROLE =====
        if (role == null) {
            setError(lblRoleError, "Role wajib dipilih.");
            valid = false;
        } else {
            lblRoleError.setVisible(false);
        }

        if (!valid) return;

        String sql = "INSERT INTO users(nama, email, password, role) VALUES (?, ?, ?, ?)";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, nama);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role);

            ps.executeUpdate();

            showInfo("Success", "Akun berhasil dibuat.");
            moveTo("login.fxml");

        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Database Error", "Gagal menyimpan user.");
        }
    }

    /** Set teks error ke label tertentu lalu tampilkan. */
    private void setError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
    }

    private void moveTo(String fxml) {
        try {

            Stage stage = (Stage) btnSignUp.getScene().getWindow();

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

    /** Tetap dipakai untuk hal yang memang butuh popup, misalnya sukses & error database. */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}