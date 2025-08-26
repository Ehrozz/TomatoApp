package com.android.tomatoapp;

public class User {
    public String fullName, username, address, email;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String fullName, String username, String address, String email) {
        this.fullName = fullName;
        this.username = username;
        this.address = address;
        this.email = email;
    }
}
