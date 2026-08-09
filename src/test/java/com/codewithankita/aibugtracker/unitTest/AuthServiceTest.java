package com.codewithankita.aibugtracker.unitTest;

import com.codewithankita.aibugtracker.Model.Role;
import com.codewithankita.aibugtracker.Model.User;
import com.codewithankita.aibugtracker.dto.AuthRequest;
import com.codewithankita.aibugtracker.dto.AuthResponse;
import com.codewithankita.aibugtracker.repository.UserRepository;
import com.codewithankita.aibugtracker.security.JwtUtil;
import com.codewithankita.aibugtracker.service.AuthService;
import com.codewithankita.aibugtracker.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_shouldCreateTesterAndReturnToken() {
        AuthRequest request = new AuthRequest();
        request.setName("Alok");
        request.setEmail("alok@test.com");
        request.setPassword("123456");

        when(userRepository.existsByEmail("alok@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtUtil.generateToken("alok@test.com", "TESTER")).thenReturn("mock-token");

        AuthResponse response = authService.signup(request);

        assertEquals("mock-token", response.getToken());
        assertEquals("TESTER", response.getRole());
        assertEquals("alok@test.com", response.getEmail());
    }

    @Test
    void signup_shouldThrowIfEmailAlreadyExists() {
        AuthRequest request = new AuthRequest();
        request.setName("Alok");
        request.setEmail("alok@test.com");
        request.setPassword("123456");

        when(userRepository.existsByEmail("alok@test.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.signup(request));

        assertEquals("Email already registered", ex.getMessage());
    }

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        User user = User.builder()
                .name("Alok")
                .email("alok@test.com")
                .password("hashed")
                .role(Role.TESTER)
                .build();

        when(userRepository.findByEmail("alok@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("alok@test.com", "TESTER")).thenReturn("mock-token");

        AuthResponse response = authService.login("alok@test.com", "123456");

        assertEquals("mock-token", response.getToken());
        assertEquals("TESTER", response.getRole());
    }

    @Test
    void login_shouldThrowForWrongPassword() {
        User user = User.builder()
                .name("Alok")
                .email("alok@test.com")
                .password("hashed")
                .role(Role.TESTER)
                .build();

        when(userRepository.findByEmail("alok@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("alok@test.com", "wrongpass"));

        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void login_shouldThrowForNonExistentEmail() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("ghost@test.com", "123456"));

        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void logout_shouldBlacklistToken() {
        authService.logout("some-token");
        verify(tokenBlacklistService, times(1)).blacklist("some-token");
    }
}
