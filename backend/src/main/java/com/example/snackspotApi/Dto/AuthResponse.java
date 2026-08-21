package com.example.snackspotApi.Dto;

import com.example.snackspotApi.Entity.Role;

public class AuthResponse {
    private boolean success;
    private String message;
    private String username;
    private Role role;

    public AuthResponse() {}

    public AuthResponse(boolean success, String message, String username, Role role) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.role = role;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
