package com.codewithankita.aibugtracker.dto;

import com.codewithankita.aibugtracker.Model.TestScriptStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TestScriptResponse {
    private UUID id;
    private UUID bugId;
    private String code;
    private TestScriptStatus status;
    private String logs;
    private LocalDateTime executedAt;
}
