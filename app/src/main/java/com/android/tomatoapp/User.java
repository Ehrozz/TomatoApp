package com.android.tomatoapp;

public class User {
    public String fullName;
    public String username;
    public String address;
    public String email;
    public String phone;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() { }

    public User(String fullName, String username, String address, String email, String phone) {
        this.fullName = fullName;
        this.username = username;
        this.address = address;
        this.email = email;
        this.phone = phone;
    }
}
