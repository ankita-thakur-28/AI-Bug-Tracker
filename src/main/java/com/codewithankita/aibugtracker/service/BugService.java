package com.codewithankita.aibugtracker.service;

import com.codewithankita.aibugtracker.Model.*;
import com.codewithankita.aibugtracker.dto.BugRequest;
import com.codewithankita.aibugtracker.dto.BugResponse;
import com.codewithankita.aibugtracker.exception.ResourceNotFoundException;
import com.codewithankita.aibugtracker.repository.BugRepository;
import com.codewithankita.aibugtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.codewithankita.aibugtracker.Model.TestScript;
import com.codewithankita.aibugtracker.Model.TestScriptStatus;
import com.codewithankita.aibugtracker.repository.TestScriptRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BugService {

    private final BugRepository bugRepository;
    private final UserRepository userRepository;
    private final TestScriptRepository testScriptRepository;
    private final AsyncAiService asyncAiService;
    private final AsyncEmailService asyncEmailService;

    public BugResponse createBug(BugRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User createdBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User assignedTo = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found"));

        if (assignedTo.getRole() != Role.DEVELOPER) {
            throw new RuntimeException("Bug can only be assigned to a Developer");
        }

        Bug bug = Bug.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .status(BugStatus.OPEN)
                .assignedTo(assignedTo)
                .createdBy(createdBy)
                .build();
        Bug saved = bugRepository.save(bug);

// Create TestScript with PENDING status immediately
        TestScript testScript = TestScript.builder()
                .bug(saved)
                .status(TestScriptStatus.PENDING)
                .build();
        TestScript savedScript = testScriptRepository.save(testScript);

// Trigger AI generation asynchronously — bug returns 201 instantly
        asyncAiService.generateAndSaveScript(saved, savedScript);

// Notify assigned developer via email
        asyncEmailService.sendBugCreatedEmail(saved, assignedTo);

        return mapToResponse(saved);
    }

    public List<BugResponse> getAllBugs(String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Bug> bugs = switch (currentUser.getRole()) {
            case ADMIN -> bugRepository.findAll();
            case DEVELOPER -> bugRepository.findByAssignedTo(currentUser);
            case TESTER -> bugRepository.findByCreatedBy(currentUser);
        };

        return bugs.stream().map(this::mapToResponse).toList();
    }

    public BugResponse getBugById(UUID id) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + id));
        return mapToResponse(bug);
    }

    public BugResponse updateBug(UUID id, BugRequest request) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + id));

        User assignedTo = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found"));

        if (assignedTo.getRole() != Role.DEVELOPER) {
            throw new RuntimeException("Bug can only be assigned to a Developer");
        }

        bug.setTitle(request.getTitle());
        bug.setDescription(request.getDescription());
        bug.setSeverity(request.getSeverity());
        bug.setAssignedTo(assignedTo);

        BugResponse response = mapToResponse(bugRepository.save(bug));

        asyncEmailService.sendBugUpdatedEmail(bug, assignedTo, bug.getCreatedBy());

        return response;
    }

    public void deleteBug(UUID id) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + id));
        bugRepository.delete(bug);
    }

    public BugResponse updateStatus(UUID id, BugStatus newStatus) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + id));

        String oldStatus = bug.getStatus().name();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User changedBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        bug.setStatus(newStatus);
        BugResponse response = mapToResponse(bugRepository.save(bug));

        asyncEmailService.sendStatusChangedEmail(bug, changedBy, oldStatus);

        return response;
    }

    public BugResponse cancelBug(UUID id, String currentUserEmail) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + id));

        if (!bug.getCreatedBy().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("You can only cancel your own bugs");
        }

        bug.setStatus(BugStatus.WITHDRAWN);
        BugResponse response = mapToResponse(bugRepository.save(bug));

        asyncEmailService.sendBugWithdrawnEmail(bug, bug.getAssignedTo());

        return response;
    }

    private BugResponse mapToResponse(Bug bug) {
        return BugResponse.builder()
                .id(bug.getId())
                .title(bug.getTitle())
                .description(bug.getDescription())
                .severity(bug.getSeverity())
                .status(bug.getStatus())
                .assignedToName(bug.getAssignedTo() != null ? bug.getAssignedTo().getName() : null)
                .assignedToEmail(bug.getAssignedTo() != null ? bug.getAssignedTo().getEmail() : null)
                .createdByName(bug.getCreatedBy().getName())
                .createdByEmail(bug.getCreatedBy().getEmail())
                .createdAt(bug.getCreatedAt())
                .updatedAt(bug.getUpdatedAt())
                .build();
    }
}
