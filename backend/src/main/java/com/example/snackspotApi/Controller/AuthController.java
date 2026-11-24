package com.example.snackspotApi.Controller;

import com.example.snackspotApi.Dto.AuthResponse;
import com.example.snackspotApi.Dto.LoginRequest;
import com.example.snackspotApi.Dto.RegisterRequest;
import com.example.snackspotApi.Dto.UserDto;
import com.example.snackspotApi.Entity.Role;
import com.example.snackspotApi.Entity.User;
import com.example.snackspotApi.Repository.UserRepository;
import com.example.snackspotApi.Security.CustomUserDetailsService;
import com.example.snackspotApi.Security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    // ✅ Register API for normal users
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.error("Username and password are required!"));
        }

        String username = request.getUsername().trim();
        String password = request.getPassword().trim();

        if (username.length() < 3) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.error("Username must be at least 3 characters long!"));
        }

        if (password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.error("Password must be at least 6 characters long!"));
        }

        if (repo.findByUsername(username) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AuthResponse.error("Username already exists!"));
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(Role.USER);

        User savedUser = repo.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                AuthResponse.success("User registered successfully!", null, savedUser.getUsername(), savedUser.getRole())
        );
    }

    // ✅ User Login API
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.error("Username and password are required!"));
        }

        String username = request.getUsername().trim();
        String password = request.getPassword().trim();

        User existingUser = repo.findByUsername(username);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error("Invalid username or password"));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error("Invalid username or password"));
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(userDetails, existingUser.getRole().name());

        return ResponseEntity.ok(
                AuthResponse.success("Login Successful", token, existingUser.getUsername(), existingUser.getRole())
        );
    }

    // ✅ Dedicated Admin Login API
    // Security: authenticate FIRST (password check), THEN verify role — prevents timing oracle leaking admin usernames
    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.error("Username and password are required!"));
        }

        String username = request.getUsername().trim();
        String password = request.getPassword().trim();

        User existingUser = repo.findByUsername(username);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error("Invalid credentials"));
        }

        // Authenticate password BEFORE revealing role to prevent user-enumeration timing oracle
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error("Invalid credentials"));
        }

        // Only after successful authentication, check if the account has ADMIN role
        if (existingUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AuthResponse.error("Access Denied: This endpoint is for admin accounts only."));
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(userDetails, existingUser.getRole().name());

        return ResponseEntity.ok(
                AuthResponse.success("Admin Login Successful", token, existingUser.getUsername(), existingUser.getRole())
        );
    }

    // ✅ Get all registered users (Admin only)
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = repo.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
