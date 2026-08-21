package com.example.snackspotApi.Controller;

import com.example.snackspotApi.Dto.AuthResponse;
import com.example.snackspotApi.Dto.UserDto;
import com.example.snackspotApi.Entity.Role;
import com.example.snackspotApi.Entity.User;
import com.example.snackspotApi.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository repo;

    // ✅ Register API for normal users
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty() ||
            user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Username and password are required!", null, null));
        }

        if (repo.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(false, "Username already exists!", null, null));
        }

        user.setRole(Role.USER);
        User savedUser = repo.save(user);
        return ResponseEntity.ok(
                new AuthResponse(true, "User registered successfully!", savedUser.getUsername(), savedUser.getRole())
        );
    }

    // ✅ User Login API
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody User user) {
        User existingUser = repo.findByUsername(user.getUsername());

        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthResponse(false, "User not found!", null, null));
        }

        if (existingUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.ok(
                    new AuthResponse(true, "Login Successful", existingUser.getUsername(), existingUser.getRole())
            );
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid Password", null, null));
        }
    }

    // ✅ Dedicated Admin Login API
    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(@RequestBody User user) {
        User existingUser = repo.findByUsername(user.getUsername());

        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthResponse(false, "Admin account not found!", null, null));
        }

        if (!existingUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid Password", null, null));
        }

        if (existingUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse(false, "Access Denied: Not an Admin account", null, null));
        }

        return ResponseEntity.ok(
                new AuthResponse(true, "Admin Login Successful", existingUser.getUsername(), existingUser.getRole())
        );
    }

    // ✅ Get all registered users (for Admin Dashboard)
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = repo.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}