package com.example.loginsharelist;


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
