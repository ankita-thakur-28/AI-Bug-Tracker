package com.codewithankita.aibugtracker.service;
import com.codewithankita.aibugtracker.Model.Role;
import com.codewithankita.aibugtracker.Model.User;
import com.codewithankita.aibugtracker.dto.AuthRequest;
import com.codewithankita.aibugtracker.dto.AuthResponse;
import com.codewithankita.aibugtracker.repository.UserRepository;
import com.codewithankita.aibugtracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthResponse signup(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.TESTER)
                .phone(request.getPhone())
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getEmail(), user.getName());
    }

    public AuthResponse login(String email, String password) {
        System.out.println(">>> [AUTH-SERVICE] Executing DB lookup for Email: " + email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ >>> [AUTH-SERVICE] FAIL: Email not found in Database: " + email);
                    return new RuntimeException("Invalid email or password");
                });

        System.out.println("✅ >>> [AUTH-SERVICE] User found in DB: " + user.getEmail() + " | Role: " + user.getRole());

        boolean isPasswordValid = passwordEncoder.matches(password, user.getPassword());
        System.out.println(">>> [AUTH-SERVICE] BCrypt Password Match Result: " + isPasswordValid);

        if (!isPasswordValid) {
            System.out.println("❌ >>> [AUTH-SERVICE] FAIL: Password mismatch for Email: " + email);
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        System.out.println("🎉 >>> [AUTH-SERVICE] SUCCESS: JWT Token generated for: " + email);
        return new AuthResponse(token, user.getRole().name(), user.getEmail(), user.getName());
    }

    public void logout(String token) {
        tokenBlacklistService.blacklist(token);
    }
}
