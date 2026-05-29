package com.codewithankita.aibugtracker.dto;
import com.codewithankita.aibugtracker.Model.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private LocalDateTime createdAt;
}

