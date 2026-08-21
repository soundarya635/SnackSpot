package com.example.snackspotApi.Config;

import com.example.snackspotApi.Entity.Role;
import com.example.snackspotApi.Entity.User;
import com.example.snackspotApi.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername(adminUsername) == null) {
            User admin = new User(adminUsername, adminPassword, Role.ADMIN);
            userRepository.save(admin);
            System.out.println("✅ Default Admin account created with username: " + adminUsername);
        }
    }
}
