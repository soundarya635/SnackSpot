package com.example.snackspotApi.Config;

import com.example.snackspotApi.Entity.Role;
import com.example.snackspotApi.Entity.User;
import com.example.snackspotApi.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username:${ADMIN_USERNAME:}}")
    private String adminUsername;

    @Value("${admin.password:${ADMIN_PASSWORD:}}")
    private String adminPassword;

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Safe migration of existing plain-text passwords
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            String currentPassword = user.getPassword();
            // Check if password is not already BCrypt hashed (BCrypt hashes start with
            // $2a$, $2b$, or $2y$ and are 60 chars)
            if (currentPassword != null && !isBCryptHashed(currentPassword)) {
                user.setPassword(passwordEncoder.encode(currentPassword));
                userRepository.save(user);
                System.out.println("🔒 Migrated user password to BCrypt hash: " + user.getUsername());
            }
        }

        // Initialize or update default Admin account only if admin credentials are
        // provided
        if (adminUsername != null && !adminUsername.trim().isEmpty() &&
                adminPassword != null && !adminPassword.trim().isEmpty()) {
            String trimmedAdminUser = adminUsername.trim();
            String trimmedAdminPass = adminPassword.trim();
            User existingAdmin = userRepository.findByUsername(trimmedAdminUser);
            if (existingAdmin == null) {
                User admin = new User(trimmedAdminUser, passwordEncoder.encode(trimmedAdminPass), Role.ADMIN);
                userRepository.save(admin);
                System.out.println("✅ Default Admin account created with username: " + trimmedAdminUser);
            } else {
                // Ensure admin role and BCrypt hash
                boolean changed = false;
                if (existingAdmin.getRole() != Role.ADMIN) {
                    existingAdmin.setRole(Role.ADMIN);
                    changed = true;
                }
                if (!isBCryptHashed(existingAdmin.getPassword())) {
                    existingAdmin.setPassword(passwordEncoder.encode(existingAdmin.getPassword()));
                    changed = true;
                }
                if (changed) {
                    userRepository.save(existingAdmin);
                }
            }
        } else {
            System.out.println(
                    "ℹ️ ADMIN_USERNAME and ADMIN_PASSWORD not set; skipping automatic admin account creation.");
        }
    }

    private boolean isBCryptHashed(String password) {
        if (password == null) {
            return false;
        }
        return (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"))
                && password.length() == 60;
    }
}
