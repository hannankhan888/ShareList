package com.example.loginsharelist;

/**
 * This class implements a User object which stores information about a user.
 */
public class User {
    public String userName;
    public String phoneNumber;
    public String emailAddress;
    public String password;

    public User() {

    }

    public User(String userName, String phoneNumber, String emailAddress, String password) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.password = password;
    }
}
