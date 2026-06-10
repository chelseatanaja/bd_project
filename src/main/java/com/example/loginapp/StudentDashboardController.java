package com.example.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class StudentDashboardController {

    @FXML
    private Label lblMyClasses;

    @FXML
    private Label lblUpcomingTasks;

    @FXML
    private Button btnLogout;

    @FXML
    private TableView<StudentAssignment> tblStudentAssignment;

    @FXML
    private TableColumn<StudentAssignment, String> colAssignmentId;

    @FXML
    private TableColumn<StudentAssignment, String> colAssignmentNama;

    @FXML
    private TableColumn<StudentAssignment, String> colKelas;

    @FXML
    private TableColumn<StudentAssignment, String> colDeadline;

    @FXML
    private TableColumn<StudentAssignment, String> colStatus;

    private final ObservableList<StudentAssignment> assignmentList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colAssignmentId.setCellValueFactory(
                new PropertyValueFactory<>("id"));

        colAssignmentNama.setCellValueFactory(
                new PropertyValueFactory<>("nama"));

        colKelas.setCellValueFactory(
                new PropertyValueFactory<>("kelas"));

        colDeadline.setCellValueFactory(
                new PropertyValueFactory<>("deadline"));

        colStatus.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        assignmentList.addAll(

                new StudentAssignment(
                        "A001",
                        "ERD Project",
                        "Basis Data A",
                        "2026-06-15",
                        "Not Submitted"
                ),

                new StudentAssignment(
                        "A002",
                        "SQL Query Task",
                        "Basis Data B",
                        "2026-06-18",
                        "Submitted"
                ),

                new StudentAssignment(
                        "A003",
                        "JavaFX CRUD App",
                        "PBO A",
                        "2026-06-20",
                        "In Progress"
                )
        );

        tblStudentAssignment.setItems(assignmentList);

        lblMyClasses.setText("4");
        lblUpcomingTasks.setText(
                String.valueOf(assignmentList.size())
        );

        btnLogout.setOnAction(
                e -> moveTo("login.fxml")
        );
    }

    private void moveTo(String fxml) {
        try {

            Stage stage = (Stage)
                    btnLogout.getScene().getWindow();

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/example/loginapp/" + fxml
                            )
                    );

            Scene scene = new Scene(loader.load());

            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class StudentAssignment {

        private final String id;
        private final String nama;
        private final String kelas;
        private final String deadline;
        private final String status;

        public StudentAssignment(
                String id,
                String nama,
                String kelas,
                String deadline,
                String status
        ) {
            this.id = id;
            this.nama = nama;
            this.kelas = kelas;
            this.deadline = deadline;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public String getNama() {
            return nama;
        }

        public String getKelas() {
            return kelas;
        }

        public String getDeadline() {
            return deadline;
        }

        public String getStatus() {
            return status;
        }
    }
}