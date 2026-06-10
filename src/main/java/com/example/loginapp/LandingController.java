package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class LandingController {

    @FXML private Button btnLogin;
    @FXML private Button btnSignUp;

    @FXML
    public void initialize() {
        btnLogin.setOnAction(e -> moveTo("login.fxml"));
        btnSignUp.setOnAction(e -> moveTo("signup.fxml"));
    }

    private void moveTo(String fxml) {
        try {

            Stage stage = (Stage) btnLogin.getScene().getWindow();

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
}