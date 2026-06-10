package com.example.loginapp;

public class Session {
    private static int userId;
    private static String nama;
    private static String email;
    private static String role;

    public static void setSession(int userId, String nama, String email, String role) {
        Session.userId = userId;
        Session.nama = nama;
        Session.email = email;
        Session.role = role;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getNama() {
        return nama;
    }

    public static String getEmail() {
        return email;
    }

    public static String getRole() {
        return role;
    }

    public static void clearSession() {
        userId = 0;
        nama = null;
        email = null;
        role = null;
    }
}
