package com.codewithankita.aibugtracker.service;

import com.codewithankita.aibugtracker.Model.Bug;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class AiService {

    @Value("${app.ai.provider-url}")
    private String providerUrl;

    @Value("${app.ai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String generatePlaywrightTest(Bug bug) {
        String prompt = buildPrompt(bug);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", "deepseek-v4-pro",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    providerUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );


            return extractCode(response.getBody());

        } catch (Exception e) {
            log.error("DeepSeek API call failed: {}", e.getMessage());
            throw new RuntimeException("AI generation failed: " + e.getMessage());
        }
    }

    private String buildPrompt(Bug bug) {
        return String.format("""
            You are a Playwright test automation expert.
            Generate a complete Playwright JavaScript test for the following bug:
            
            Title: %s
            Description: %s
            Severity: %s
            
            Requirements:
            - Use Playwright with JavaScript
            - Test should verify the bug scenario
            - Include proper assertions
            - Use async/await
            - Target URL: http://localhost:3000
            - Return ONLY the raw JavaScript code
            - No explanations, no markdown, no backticks
            """,
                bug.getTitle(),
                bug.getDescription(),
                bug.getSeverity()
        );
    }

    private String extractCode(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();


            // Strip markdown code blocks if present
            content = content.replaceAll("```javascript\\n?", "");
            content = content.replaceAll("```js\\n?", "");
            content = content.replaceAll("```\\n?", "");

            return content.trim();

        } catch (Exception e) {
            log.error("Failed to parse DeepSeek response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse AI response");
        }
    }
}