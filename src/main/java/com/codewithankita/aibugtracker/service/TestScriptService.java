package com.codewithankita.aibugtracker.service;

import com.codewithankita.aibugtracker.Model.Bug;
import com.codewithankita.aibugtracker.Model.TestScript;
import com.codewithankita.aibugtracker.Model.TestScriptStatus;
import com.codewithankita.aibugtracker.Model.User;
import com.codewithankita.aibugtracker.dto.TestScriptResponse;
import com.codewithankita.aibugtracker.exception.ResourceNotFoundException;
import com.codewithankita.aibugtracker.repository.BugRepository;
import com.codewithankita.aibugtracker.repository.TestScriptRepository;
import com.codewithankita.aibugtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestScriptService {

    private final TestScriptRepository testScriptRepository;
    private final BugRepository bugRepository;
    private final UserRepository userRepository;

    public TestScriptResponse getTestScriptByBugId(UUID bugId, String currentUserEmail) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + bugId));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TestScript testScript = testScriptRepository.findByBug(bug)
                .orElseGet(() -> TestScript.builder()
                        .bug(bug)
                        .code("// AI test generation in progress...")
                        .status(TestScriptStatus.PENDING)
                        .logs("Generating script...")
                        .build());

        return mapToResponse(testScript);
    }

    private TestScriptResponse mapToResponse(TestScript testScript) {
        return TestScriptResponse.builder()
                .id(testScript.getId())
                .bugId(testScript.getBug().getId())
                .code(testScript.getCode())
                .status(testScript.getStatus())
                .logs(testScript.getLogs())
                .executedAt(testScript.getExecutedAt())
                .build();
    }
}
