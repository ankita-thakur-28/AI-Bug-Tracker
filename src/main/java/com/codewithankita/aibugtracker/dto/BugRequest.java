package com.codewithankita.aibugtracker.dto;

import com.codewithankita.aibugtracker.Model.Severity;
import com.codewithankita.aibugtracker.validation.SafeText;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BugRequest {

    @NotBlank(message = "Title is required")
    @SafeText
    private String title;

    @NotBlank(message = "Description is required")
    @SafeText
    private String description;

    @NotNull(message = "Severity is required")
    private Severity severity;

    @NotNull(message = "Assigned developer is required")
    private UUID assignedToId;
}
