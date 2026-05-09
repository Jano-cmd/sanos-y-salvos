package com.example.authservice.service;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.exception.InvalidCredentialsException;
import com.example.authservice.exception.ResourceAlreadyExistsException;
import com.example.authservice.model.User;
import com.example.authservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        String email = normalize(request.getEmail());
        String name = normalize(request.getName());
        String password = request.getPassword();

        if (email == null || name == null || password == null || password.isBlank()) {
            throw new IllegalArgumentException("name, email and password are required");
        }

        if (userRepository.findByEmail(email) != null) {
            throw new ResourceAlreadyExistsException("User already exists with email: " + email);
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public Map<String, Object> login(LoginRequest request) {
        String email = normalize(request.getEmail());
        String password = request.getPassword();

        if (email == null || password == null || password.isBlank()) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // In a real application, we would generate a JWT token here.
        // For simplicity, we are returning a mock token and user info.
        Map<String, Object> response = new HashMap<>();
        response.put("token", "mock-jwt-token");
        response.put("user", user);
        return response;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

}
