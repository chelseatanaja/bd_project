package com.example.bd1_2;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
        public static Connection databaseLink;

        public static Connection getConnection(){
            String databaseName = "dbGenap";
            String user = "postgres";
            String password = "chelsea10";
            String url = "jdbc:postgresql://localhost:5432/" + databaseName;

            try {
                Class.forName("org.postgresql.Driver");
                databaseLink = DriverManager.getConnection(url, user, password);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return  databaseLink;
        }

        public static Statement getStatement() throws SQLException {
            return  getConnection().createStatement();
        }
}
