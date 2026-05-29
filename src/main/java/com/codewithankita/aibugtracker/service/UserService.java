package com.codewithankita.aibugtracker.service;
import com.codewithankita.aibugtracker.Model.User;
import com.codewithankita.aibugtracker.dto.UpdateUserRequest;
import com.codewithankita.aibugtracker.dto.UserResponse;
import com.codewithankita.aibugtracker.exception.ResourceNotFoundException;
import com.codewithankita.aibugtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        return mapToResponse(userRepository.save(user));
    }

    public void deleteUser(UUID id) {
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getEmail().equals(currentEmail)) {
            throw new RuntimeException("You cannot delete your own account");
        }

        userRepository.delete(user);
    }

    public void changePassword(String currentPassword, String newPassword) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (currentPassword.equals(newPassword)) {
            throw new RuntimeException("New password must be different from current password");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

