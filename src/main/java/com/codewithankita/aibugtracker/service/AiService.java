package com.codewithankita.aibugtracker.service;

import com.codewithankita.aibugtracker.Model.Bug;
import com.codewithankita.aibugtracker.Model.User;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    @Value("${app.ai.provider-url}")
    private String providerUrl;

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.model}")
    private String model;

    @Value("${app.ai.temperature}")
    private double temperature;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String generatePlaywrightTest(Bug bug) {
        String prompt = buildPrompt(bug);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", temperature
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
            Generate a standalone executable Node.js Playwright test script for the following bug report:
            
            Title: %s
            Description: %s
            Severity: %s
            
            Requirements:
            - Use standalone Playwright with Node.js: const { chromium } = require('playwright');
            - Must use async IIFE: (async () => { const browser = await chromium.launch({ headless: true }); ... })();
            - Target URL: http://localhost:3002
            - Test the exact scenario described in the bug report using page methods (page.goto, page.click, page.fill, page.locator, etc.).
            - Include assertions. If test fails, throw new Error("Test Failed: <reason>"). If passed, console.log("TEST PASSED").
            - Always close browser in a try...finally block.
            - Return ONLY raw valid JavaScript code. No markdown, no backticks, no explanations.
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

    public UUID recommendDeveloper(String title, String description, List<User> developers) {
        StringBuilder devList = new StringBuilder();
        for (User d : developers) {
            devList.append(String.format("- ID: %s, Name: %s, Email: %s\n", d.getId().toString(), d.getName(), d.getEmail()));
        }

        String prompt = String.format("""
            You are a development lead/triage manager.
            Your task is to assign the following bug to the most appropriate developer based on their name/specialty.
            
            Bug Title: %s
            Bug Description: %s
            
            Available Developers:
            %s
            
            Instructions:
            - Analyze the bug title and description to see if it targets frontend/UI, backend/database, testing/automation, or general issues.
            - Match it to the developer whose specialty or name best matches the category.
            - Return ONLY the exact UUID of the chosen developer.
            - Do not include any explanations, formatting, markdown, or comments. Just the UUID string.
            """,
                title,
                description,
                devList.toString()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.1
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    providerUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String content = extractRecommendationContent(response.getBody());
            log.info("AI raw recommendation output: {}", content);
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}").matcher(content);
            if (matcher.find()) {
                UUID uuid = UUID.fromString(matcher.group());
                log.info("AI recommended developer UUID extracted: {}", uuid);
                return uuid;
            }
            log.warn("Could not find UUID pattern in AI recommendation output. Using fallback.");
            if (!developers.isEmpty()) {
                return developers.get(0).getId();
            }
            throw new RuntimeException("AI recommendation output invalid and no developers available");
        } catch (Exception e) {
            log.error("DeepSeek API call for developer recommendation failed: {}", e.getMessage());
            if (!developers.isEmpty()) {
                log.info("Falling back to first developer: {}", developers.get(0).getEmail());
                return developers.get(0).getId();
            }
            throw new RuntimeException("AI recommendation failed and no fallback available", e);
        }
    }

    private String extractRecommendationContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
            return content.replaceAll("`", "").trim();
        } catch (Exception e) {
            log.error("Failed to parse DeepSeek response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse AI response");
        }
    }
}