package com.codewithankita.aibugtracker.unitTest;

import com.codewithankita.aibugtracker.Model.Bug;
import com.codewithankita.aibugtracker.Model.TestScript;
import com.codewithankita.aibugtracker.Model.TestScriptStatus;
import com.codewithankita.aibugtracker.repository.TestScriptRepository;
import com.codewithankita.aibugtracker.service.PlaywrightRunnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaywrightRunnerServiceTest {

    @Mock
    private TestScriptRepository testScriptRepository;

    @Captor
    private ArgumentCaptor<TestScript> testScriptCaptor;

    private PlaywrightRunnerService playwrightRunnerService;

    @BeforeEach
    void setUp() {
        playwrightRunnerService = new PlaywrightRunnerService(testScriptRepository);
    }

    @Test
    void runTest_shouldSetPassForSuccessfulScript() {
        Bug bug = Bug.builder().id(UUID.randomUUID()).build();
        TestScript testScript = TestScript.builder()
                .bug(bug)
                .code("console.log('test passed'); process.exit(0);")
                .status(TestScriptStatus.PENDING)
                .build();

        playwrightRunnerService.runTest(testScript);

        verify(testScriptRepository, atLeastOnce()).save(testScriptCaptor.capture());
        TestScript saved = testScriptCaptor.getValue();

        assertEquals(TestScriptStatus.PASS, saved.getStatus());
        assertNotNull(saved.getExecutedAt());
    }

    @Test
    void runTest_shouldSetFailForFailingScript() {
        Bug bug = Bug.builder().id(UUID.randomUUID()).build();
        TestScript testScript = TestScript.builder()
                .bug(bug)
                .code("console.log('test failed'); process.exit(1);")
                .status(TestScriptStatus.PENDING)
                .build();

        playwrightRunnerService.runTest(testScript);

        verify(testScriptRepository, atLeastOnce()).save(testScriptCaptor.capture());
        TestScript saved = testScriptCaptor.getValue();

        assertEquals(TestScriptStatus.FAIL, saved.getStatus());
        assertNotNull(saved.getExecutedAt());
    }

    @Test
    void runTest_shouldSkipWhenCodeIsNull() {
        Bug bug = Bug.builder().id(UUID.randomUUID()).build();
        TestScript testScript = TestScript.builder()
                .bug(bug)
                .code(null)
                .status(TestScriptStatus.PENDING)
                .build();

        playwrightRunnerService.runTest(testScript);

        verify(testScriptRepository, never()).save(any());
    }

    @Test
    void runTest_shouldSetFailForInvalidCode() {
        Bug bug = Bug.builder().id(UUID.randomUUID()).build();
        TestScript testScript = TestScript.builder()
                .bug(bug)
                .code("this is not valid javascript $$$")
                .status(TestScriptStatus.PENDING)
                .build();

        playwrightRunnerService.runTest(testScript);

        verify(testScriptRepository, atLeastOnce()).save(testScriptCaptor.capture());
        TestScript saved = testScriptCaptor.getValue();

        assertEquals(TestScriptStatus.FAIL, saved.getStatus());
        assertNotNull(saved.getLogs());
        assertFalse(saved.getLogs().isEmpty());
    }
}
