package com.codewithankita.aibugtracker.service;

import com.codewithankita.aibugtracker.Model.Bug;
import com.codewithankita.aibugtracker.Model.TestScript;
import com.codewithankita.aibugtracker.Model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${app.brevo.api-key}")
    private String apiKey;

    @Value("${app.brevo.sender-email}")
    private String senderEmail;

    @Value("${app.brevo.sender-name}")
    private String senderName;

    private final RestTemplate restTemplate;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    public void sendEmail(String toEmail, String toName, String subject, String htmlContent) {
        if (apiKey == null || apiKey.isBlank() || "YOUR_BREVO_KEY_HERE".equals(apiKey)) {
            log.warn("Brevo API key not configured. Skipping email to {}: {}", toEmail, subject);
            return;
        }

        Map<String, Object> body = Map.of(
                "sender", Map.of("email", senderEmail, "name", senderName),
                "to", List.of(Map.of("email", toEmail, "name", toName)),
                "subject", subject,
                "htmlContent", htmlContent
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        try {
            restTemplate.exchange(
                    BREVO_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            log.info("Email sent to {}: {}", toEmail, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendBugCreatedEmail(Bug bug, User assignedTo) {
        String subject = "[AIBT] New Bug Assigned: " + bug.getTitle();
        String html = String.format("""
                <h2>New Bug Assigned to You</h2>
                <table style="border-collapse:collapse;">
                  <tr><td style="padding:8px;font-weight:bold;">Title</td><td style="padding:8px;">%s</td></tr>
                  <tr><td style="padding:8px;font-weight:bold;">Severity</td><td style="padding:8px;">%s</td></tr>
                  <tr><td style="padding:8px;font-weight:bold;">Description</td><td style="padding:8px;">%s</td></tr>
                  <tr><td style="padding:8px;font-weight:bold;">Reported by</td><td style="padding:8px;">%s</td></tr>
                </table>
                <p><a href="http://localhost:3000/bugs/%s">View Bug</a></p>
                """,
                escapeHtml(bug.getTitle()),
                bug.getSeverity(),
                escapeHtml(bug.getDescription()),
                bug.getCreatedBy().getName(),
                bug.getId()
        );
        sendEmail(assignedTo.getEmail(), assignedTo.getName(), subject, html);
    }

    public void sendBugUpdatedEmail(Bug bug, User assignedTo, User createdBy) {
        String subject = "[AIBT] Bug Updated: " + bug.getTitle();
        String html = String.format("""
                <h2>Bug Details Updated</h2>
                <p>Bug <strong>%s</strong> has been updated by Admin.</p>
                <table style="border-collapse:collapse;">
                  <tr><td style="padding:8px;font-weight:bold;">Severity</td><td style="padding:8px;">%s</td></tr>
                  <tr><td style="padding:8px;font-weight:bold;">Status</td><td style="padding:8px;">%s</td></tr>
                  <tr><td style="padding:8px;font-weight:bold;">Description</td><td style="padding:8px;">%s</td></tr>
                </table>
                <p><a href="http://localhost:3000/bugs/%s">View Bug</a></p>
                """,
                escapeHtml(bug.getTitle()),
                bug.getSeverity(),
                bug.getStatus(),
                escapeHtml(bug.getDescription()),
                bug.getId()
        );
        sendEmail(assignedTo.getEmail(), assignedTo.getName(), subject, html);
        sendEmail(createdBy.getEmail(), createdBy.getName(), subject + " (Tester)", html);
    }

    public void sendStatusChangedEmail(Bug bug, User changedBy, String oldStatus) {
        String subject = "[AIBT] Bug Status Changed: " + bug.getTitle();
        String html = String.format("""
                <h2>Bug Status Updated</h2>
                <p><strong>%s</strong> changed status of bug <strong>%s</strong></p>
                <table style="border-collapse:collapse;">
                  <tr><td style="padding:8px;font-weight:bold;">Previous Status</td><td style="padding:8px;">%s</td></tr>
                  <tr><td style="padding:8px;font-weight:bold;">New Status</td><td style="padding:8px;background:#e8f5e9;">%s</td></tr>
                </table>
                <p><a href="http://localhost:3000/bugs/%s">View Bug</a></p>
                """,
                changedBy.getName(),
                escapeHtml(bug.getTitle()),
                oldStatus,
                bug.getStatus(),
                bug.getId()
        );
        sendEmail(bug.getCreatedBy().getEmail(), bug.getCreatedBy().getName(), subject, html);
    }

    public void sendBugWithdrawnEmail(Bug bug, User assignedTo) {
        String subject = "[AIBT] Bug Withdrawn: " + bug.getTitle();
        String html = String.format("""
                <h2>Bug Withdrawn by Tester</h2>
                <p>Bug <strong>%s</strong> has been withdrawn by <strong>%s</strong>.</p>
                <table style="border-collapse:collapse;">
                  <tr><td style="padding:8px;font-weight:bold;">Severity</td><td style="padding:8px;">%s</td></tr>
                  <tr><td style="padding:8px;font-weight:bold;">Description</td><td style="padding:8px;">%s</td></tr>
                </table>
                """,
                escapeHtml(bug.getTitle()),
                bug.getCreatedBy().getName(),
                bug.getSeverity(),
                escapeHtml(bug.getDescription())
        );
        sendEmail(assignedTo.getEmail(), assignedTo.getName(), subject, html);
    }

    public void sendTestResultEmail(Bug bug, TestScript testScript, User createdBy) {
        String badge = testScript.getStatus() == com.codewithankita.aibugtracker.Model.TestScriptStatus.PASS
                ? "<span style='color:green;font-weight:bold;'>PASS</span>"
                : "<span style='color:red;font-weight:bold;'>FAIL</span>";
        String subject = "[AIBT] Test Result: " + badge + " — " + bug.getTitle();
        String logs = testScript.getLogs() != null && testScript.getLogs().length() > 500
                ? testScript.getLogs().substring(0, 500) + "..."
                : testScript.getLogs();
        String html = String.format("""
                <h2>AI Test Execution Result</h2>
                <p>Bug: <strong>%s</strong></p>
                <p>Result: %s</p>
                <p>Executed at: %s</p>
                <h3>Execution Logs</h3>
                <pre style="background:#f5f5f5;padding:12px;border-radius:4px;overflow-x:auto;">%s</pre>
                <p><a href="http://localhost:3000/bugs/%s">View Bug</a></p>
                """,
                escapeHtml(bug.getTitle()),
                badge,
                testScript.getExecutedAt(),
                escapeHtml(logs),
                bug.getId()
        );
        sendEmail(createdBy.getEmail(), createdBy.getName(), subject, html);
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
