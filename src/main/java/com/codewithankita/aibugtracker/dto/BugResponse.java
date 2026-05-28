package com.codewithankita.aibugtracker.dto;

import com.codewithankita.aibugtracker.Model.BugStatus;
import com.codewithankita.aibugtracker.Model.Severity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BugResponse {
    private UUID id;
    private String title;
    private String description;
    private Severity severity;
    private BugStatus status;
    private String assignedToName;
    private String assignedToEmail;
    private String createdByName;
    private String createdByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}