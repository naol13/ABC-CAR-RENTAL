package com.example.carrentalmanagementsystem.dao;

import com.example.carrentalmanagementsystem.model.User;

import java.util.List;

public interface UserDAO {
    User getUserByUsername(String username);
    List<User> getAllUsers();
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(int userId);
}
