package com.example.snackspotApi.Dto;

import com.example.snackspotApi.Entity.Role;

public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private String username;
    private Role role;

    public AuthResponse() {}

    public AuthResponse(boolean success, String message, String token, String username, Role role) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.username = username;
        this.role = role;
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(false, message, null, null, null);
    }

    public static AuthResponse success(String message, String token, String username, Role role) {
        return new AuthResponse(true, message, token, username, role);
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
