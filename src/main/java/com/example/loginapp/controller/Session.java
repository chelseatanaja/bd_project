package com.example.loginapp.controller;


public class Session {
    private static int userId;
    private static String nama;
    private static String email;
    private static String role;


    private static String currentKelasId;
    private static String currentKelasNama;


    public static void setSession(int userId, String nama, String email, String role) {
        Session.userId = userId;
        Session.nama = nama;
        Session.email = email;
        Session.role = role;
    }


    // Setter
    public static void setUserId(int id)             { userId = id; }
    public static void setNama(String n)             { nama = n; }
    public static void setEmail(String e)            { email = e; }
    public static void setRole(String r)             { role = r; }
    public static void setCurrentKelasId(String id)  { currentKelasId = id; }
    public static void setCurrentKelasNama(String n) { currentKelasNama = n; }


    // Getter
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
    public static String getCurrentKelasId()   { return currentKelasId; }
    public static String getCurrentKelasNama() { return currentKelasNama; }




    public static void clearSession() {
        userId = 0;
        nama = null;
        email = null;
        role = null;
    }
}

