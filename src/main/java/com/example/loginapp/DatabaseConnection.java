package com.example.loginapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String DATABASE_NAME = "ProjekBD";
    private static final String USER = "postgres";
    private static final String PASSWORD = "chelsea10";
    private static final String URL = "jdbc:postgresql://localhost:5432/" + DATABASE_NAME;

    private static Connection databaseLink;

    public static Connection getConnection() {
        try {
            if (databaseLink == null || databaseLink.isClosed()) {
                Class.forName("org.postgresql.Driver");
                databaseLink = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database Connected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return databaseLink;
    }

    public static Statement getStatement() throws SQLException {
        return getConnection().createStatement();
    }

    public static void closeConnection() {
        try {
            if (databaseLink != null && !databaseLink.isClosed()) {
                databaseLink.close();
                System.out.println("Database Closed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}