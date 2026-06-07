package com.codewithankita.aibugtracker.service;

import com.codewithankita.aibugtracker.Model.Bug;
import com.codewithankita.aibugtracker.Model.TestScript;
import com.codewithankita.aibugtracker.Model.TestScriptStatus;
import com.codewithankita.aibugtracker.repository.TestScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncAiService {

    private final AiService aiService;
    private final TestScriptRepository testScriptRepository;

    @Async("taskExecutor")
    public void generateAndSaveScript(Bug bug, TestScript testScript) {
        try {
            log.info("Starting AI generation for bug: {}", bug.getId());

            String code = aiService.generatePlaywrightTest(bug);

            testScript.setCode(code);
            testScript.setStatus(TestScriptStatus.PENDING);
            testScriptRepository.save(testScript);

            log.info("AI generation complete for bug: {}", bug.getId());

        } catch (Exception e) {
            log.error("AI generation failed for bug: {}", bug.getId(), e);
            testScript.setStatus(TestScriptStatus.AI_FAILED);
            testScript.setLogs(e.getMessage());
            testScriptRepository.save(testScript);
        }
    }
}
