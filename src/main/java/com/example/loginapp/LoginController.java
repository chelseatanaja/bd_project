package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Hyperlink btnSignUp;

    @FXML
    public void initialize() {
        btnLogin.setOnAction(e -> login());
        btnSignUp.setOnAction(e -> moveTo("signup.fxml"));
    }

    private void login() {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Login Failed", "Email dan password wajib diisi.");
            return;
        }

        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");

                if (role.equalsIgnoreCase("Admin")) {
                    moveTo("admin_dashboard.fxml");
                } else if (role.equalsIgnoreCase("Dosen")) {
                    moveTo("dosen_dashboard.fxml");
                } else {
                    moveTo("student_dashboard.fxml");
                }

            } else {
                showAlert("Login Failed", "Email atau password salah.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Gagal login ke database.");
        }
    }

    private void moveTo(String fxml) {
        try {
            String path = "/com/example/loginapp/" + fxml;
            java.net.URL url = getClass().getResource(path);

            System.out.println("Mencari FXML: " + path);
            System.out.println("Hasil URL: " + url);

            if (url == null) {
                showAlert("FXML Error", "File tidak ditemukan:\n" + path);
                return;
            }

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(url);

            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}