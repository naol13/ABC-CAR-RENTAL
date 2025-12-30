package com.example.carrentalmanagementsystem.util;

import com.example.carrentalmanagementsystem.model.User;

public class SessionManager {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isAdmin() {
        return isLoggedIn() && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isStaff() {
        return isLoggedIn() && "staff".equalsIgnoreCase(currentUser.getRole());
    }
}
