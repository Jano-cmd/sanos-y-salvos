package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class LoginRequest {

    @JsonAlias("username")
    private String email;

    private String password;

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
