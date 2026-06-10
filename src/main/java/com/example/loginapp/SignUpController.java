package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SignUpController {

    @FXML private TextField txtNama;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Button btnSignUp;
    @FXML private Hyperlink btnLogin;

    @FXML
    public void initialize() {
        cmbRole.getItems().addAll("Admin", "Dosen", "Student");
        btnSignUp.setOnAction(e -> signUp());
        btnLogin.setOnAction(e -> moveTo("login.fxml"));
    }

    private void signUp() {
        String nama = txtNama.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        String role = cmbRole.getValue();

        if (nama.isEmpty() || email.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty() || role == null) {
            showAlert("Sign Up Failed", "Semua field wajib diisi.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Sign Up Failed", "Password tidak sama.");
            return;
        }

        String sql = "INSERT INTO users(nama, email, password, role) VALUES (?, ?, ?, ?)";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, nama);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role);

            ps.executeUpdate();

            showAlert("Success", "Akun berhasil dibuat.");
            moveTo("login.fxml");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Gagal menyimpan user.");
        }
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}