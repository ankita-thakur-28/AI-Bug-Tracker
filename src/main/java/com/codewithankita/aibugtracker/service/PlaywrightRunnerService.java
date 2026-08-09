package com.codewithankita.aibugtracker.service;

import com.codewithankita.aibugtracker.Model.TestScript;
import com.codewithankita.aibugtracker.Model.TestScriptStatus;
import com.codewithankita.aibugtracker.repository.TestScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaywrightRunnerService {

    private final TestScriptRepository testScriptRepository;
    private final AsyncEmailService asyncEmailService;

    private boolean isDockerAvailable = false;

    @PostConstruct
    public void checkRuntime() {
        // Verify local Node.js
        try {
            Process process = new ProcessBuilder("which", "node").start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished || process.exitValue() != 0) {
                log.warn("Node.js is not available in PATH. Local Playwright tests will fail.");
            } else {
                String path = new String(process.getInputStream().readAllBytes()).trim();
                log.info("Node.js found at: {}", path);
            }
        } catch (Exception e) {
            log.warn("Could not verify Node.js runtime: {}", e.getMessage());
        }

        // Verify Docker availability
        try {
            Process process = new ProcessBuilder("docker", "info").start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (finished && process.exitValue() == 0) {
                isDockerAvailable = true;
                log.info("Docker daemon is running. Sandboxed execution enabled.");
            } else {
                log.warn("Docker daemon is not running or not available. Running unsandboxed fallback.");
            }
        } catch (Exception e) {
            log.warn("Could not verify Docker runtime: {}", e.getMessage());
        }
    }

    public void runTest(TestScript testScript) {
        if (testScript.getCode() == null || testScript.getCode().isBlank()) {
            log.warn("No code to run for script {}", testScript.getId());
            return;
        }

        Path tempDir = Path.of("/tmp/aibt");
        Path tempFile = tempDir.resolve(testScript.getBug().getId() + ".spec.js");

        try {
            Files.createDirectories(tempDir);
            Files.writeString(tempFile, testScript.getCode());

            log.info("Executing Playwright test for bug: {}", testScript.getBug().getId());

            ProcessBuilder pb;
            if (isDockerAvailable) {
                log.info("Running sandboxed Playwright test inside Docker container.");
                pb = new ProcessBuilder(
                        "docker", "run", "--rm",
                        "--network", "host",
                        "-v", "/tmp/aibt:/tmp/aibt",
                        "-w", "/tmp/aibt",
                        "mcr.microsoft.com/playwright:v1.45.0-jammy",
                        "node", tempFile.toString()
                );
            } else {
                log.warn("WARNING: Running Playwright test UNSANDBOXED on host system!");
                pb = new ProcessBuilder("node", tempFile.toString());
                pb.directory(tempDir.toFile());
            }
            pb.redirectErrorStream(true);

            Process process = pb.start();
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            if (!finished) {
                process.destroyForcibly();
                testScript.setStatus(TestScriptStatus.FAIL);
                testScript.setLogs("Execution timed out after 30 seconds");
            } else if (process.exitValue() == 0) {
                testScript.setStatus(TestScriptStatus.PASS);
                testScript.setLogs(output);
            } else {
                testScript.setStatus(TestScriptStatus.FAIL);
                testScript.setLogs(output);
            }

            testScript.setExecutedAt(LocalDateTime.now());
            testScriptRepository.save(testScript);

            log.info("Test result for bug {}: {}", testScript.getBug().getId(), testScript.getStatus());
            asyncEmailService.sendTestResultEmail(testScript.getBug(), testScript, testScript.getBug().getCreatedBy());

        } catch (IOException e) {
            log.error("Failed to write or execute test script for bug: {}", testScript.getBug().getId(), e);
            testScript.setStatus(TestScriptStatus.FAIL);
            testScript.setLogs("I/O error: " + e.getMessage());
            testScript.setExecutedAt(LocalDateTime.now());
            testScriptRepository.save(testScript);
            asyncEmailService.sendTestResultEmail(testScript.getBug(), testScript, testScript.getBug().getCreatedBy());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Test execution interrupted for bug: {}", testScript.getBug().getId(), e);
            testScript.setStatus(TestScriptStatus.FAIL);
            testScript.setLogs("Execution interrupted: " + e.getMessage());
            testScript.setExecutedAt(LocalDateTime.now());
            testScriptRepository.save(testScript);
            asyncEmailService.sendTestResultEmail(testScript.getBug(), testScript, testScript.getBug().getCreatedBy());

        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                log.warn("Failed to delete temp file: {}", tempFile, e);
            }
        }
    }
}
