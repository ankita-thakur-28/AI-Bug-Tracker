package com.codewithankita.aibugtracker.controller;

import com.codewithankita.aibugtracker.dto.TestScriptResponse;
import com.codewithankita.aibugtracker.service.TestScriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
public class TestScriptController {

    private final TestScriptService testScriptService;

    @GetMapping("/{bugId}/test-result")
    @PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','TESTER')")
    public ResponseEntity<TestScriptResponse> getTestResult(@PathVariable UUID bugId,
                                                             Authentication auth) {
        return ResponseEntity.ok(testScriptService.getTestScriptByBugId(bugId, auth.getName()));
    }
}
