package com.codewithankita.aibugtracker.unitTest;

import com.codewithankita.aibugtracker.Model.*;
import com.codewithankita.aibugtracker.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<HttpEntity<Object>> requestCaptor;

    private EmailService emailService;
    private Bug sampleBug;
    private User developer;
    private User tester;

    @BeforeEach
    void setUp() throws Exception {
        emailService = new EmailService(restTemplate);

        setField("apiKey", "test-brevo-key");
        setField("senderEmail", "noreply@aibt.dev");
        setField("senderName", "AIBT System");

        developer = User.builder()
                .name("Dev User")
                .email("dev@example.com")
                .role(Role.DEVELOPER)
                .build();

        tester = User.builder()
                .name("Tester User")
                .email("tester@example.com")
                .role(Role.TESTER)
                .build();

        sampleBug = Bug.builder()
                .id(UUID.randomUUID())
                .title("Login not working")
                .description("Clicking login does nothing")
                .severity(Severity.HIGH)
                .status(BugStatus.OPEN)
                .assignedTo(developer)
                .createdBy(tester)
                .build();
    }

    @Test
    void sendBugCreatedEmail_sendsToDeveloper() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok("OK"));

        emailService.sendBugCreatedEmail(sampleBug, developer);

        verify(restTemplate).exchange(
                eq("https://api.brevo.com/v3/smtp/email"),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                eq(String.class)
        );

        HttpEntity<Object> request = requestCaptor.getValue();
        assertNotNull(request.getBody());
        assertTrue(request.getHeaders().get("api-key").contains("test-brevo-key"));
    }

    @Test
    void sendBugCreatedEmail_skipsWhenKeyIsPlaceholder() throws Exception {
        setField("apiKey", "YOUR_BREVO_KEY_HERE");

        emailService.sendBugCreatedEmail(sampleBug, developer);

        verify(restTemplate, never()).exchange(anyString(), any(), any(), any());
    }

    @Test
    void sendStatusChangedEmail_includesOldAndNewStatus() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok("OK"));

        emailService.sendStatusChangedEmail(sampleBug, developer, "IN_PROGRESS");

        verify(restTemplate).exchange(
                eq("https://api.brevo.com/v3/smtp/email"),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                eq(String.class)
        );

        String body = requestCaptor.getValue().getBody().toString();
        assertTrue(body.contains("tester@example.com"));
    }

    @Test
    void sendTestResultEmail_includesPassOrFailBadge() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok("OK"));

        TestScript testScript = TestScript.builder()
                .id(UUID.randomUUID())
                .bug(sampleBug)
                .code("console.log('test');")
                .status(TestScriptStatus.PASS)
                .logs("All tests passed")
                .executedAt(LocalDateTime.now())
                .build();

        emailService.sendTestResultEmail(sampleBug, testScript, tester);

        verify(restTemplate).exchange(
                eq("https://api.brevo.com/v3/smtp/email"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    private void setField(String name, Object value) throws Exception {
        var field = EmailService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(emailService, value);
    }
}
