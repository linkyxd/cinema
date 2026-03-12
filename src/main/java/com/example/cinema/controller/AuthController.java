package com.example.cinema.controller;

import com.example.cinema.model.UserAccount;
import com.example.cinema.repository.UserAccountRepository;
import com.example.cinema.service.AuthTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public AuthController(UserAccountRepository userRepo, PasswordEncoder passwordEncoder,
                          AuthTokenService authTokenService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
    }

    public static class RegistrationRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        @NotBlank
        private String fullName;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserAccount register(@Valid @RequestBody RegistrationRequest request) {
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken");
        }

        if (!isStrongPassword(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password is too weak. Min 8 chars, digit, letter and special symbol required");
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // роль по умолчанию ROLE_USER, не устанавливаем явно
        return userRepo.save(user);
    }

    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        UserAccount user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return authTokenService.createTokenPair(user);
    }

    public static class RefreshRequest {
        @NotBlank
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    @PostMapping("/refresh")
    public Map<String, String> refresh(@Valid @RequestBody RefreshRequest request) {
        return authTokenService.refreshTokens(request.getRefreshToken());
    }

    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = Pattern.compile("[A-Za-z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("\\d").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[^A-Za-z0-9]").matcher(password).find();
        return hasLetter && hasDigit && hasSpecial;
    }
}

