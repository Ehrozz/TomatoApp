package com.android.tomatoapp.auth.data;

public class User {
    public String fullName;
    public String username;
    public String usernameLower;
    public String address;
    public String email;
    public String photoUri;
    public String phone;
    public String phoneInternational;
    public String phoneLocal;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() { }

    public User(String fullName,
                String username,
                String usernameLower,
                String address,
                String email,
                String phone,
                String phoneInternational,
                String phoneLocal) {
        this.fullName = fullName;
        this.username = username;
        this.usernameLower = usernameLower;
        this.address = address;
        this.email = email;
        this.photoUri = null;
        this.phone = phone;
        this.phoneInternational = phoneInternational;
        this.phoneLocal = phoneLocal;
    }
}
