package com.codewithankita.aibugtracker.controller;

import com.codewithankita.aibugtracker.Model.BugStatus;
import com.codewithankita.aibugtracker.dto.BugRequest;
import com.codewithankita.aibugtracker.dto.BugResponse;
import com.codewithankita.aibugtracker.service.BugService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TESTER')")
    public ResponseEntity<BugResponse> createBug(@Valid @RequestBody BugRequest request) {
        return ResponseEntity.status(201).body(bugService.createBug(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','TESTER')")
    public ResponseEntity<List<BugResponse>> getAllBugs(Authentication auth) {
        return ResponseEntity.ok(bugService.getAllBugs(auth.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEVELOPER','TESTER')")
    public ResponseEntity<BugResponse> getBugById(@PathVariable UUID id) {
        return ResponseEntity.ok(bugService.getBugById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BugResponse> updateBug(@PathVariable UUID id,
                                                 @Valid @RequestBody BugRequest request) {
        return ResponseEntity.ok(bugService.updateBug(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteBug(@PathVariable UUID id) {
        bugService.deleteBug(id);
        return ResponseEntity.ok("Bug deleted successfully");
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<BugResponse> updateStatus(@PathVariable UUID id,
                                                    @RequestBody Map<String, String> body) {
        BugStatus status = BugStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(bugService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TESTER')")
    public ResponseEntity<BugResponse> cancelBug(@PathVariable UUID id,
                                                 Authentication auth) {
        return ResponseEntity.ok(bugService.cancelBug(id, auth.getName()));
    }
}