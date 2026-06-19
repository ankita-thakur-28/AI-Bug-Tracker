package com.codewithankita.aibugtracker.unitTest;

import com.codewithankita.aibugtracker.Model.Bug;
import com.codewithankita.aibugtracker.Model.Severity;
import com.codewithankita.aibugtracker.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private AiService aiService;
    private Bug sampleBug;

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper realMapper = new ObjectMapper();
        aiService = new AiService(restTemplate, realMapper);

        sampleBug = Bug.builder()
                .title("Login button not working")
                .description("Clicking login does nothing")
                .severity(Severity.HIGH)
                .build();

        setField("providerUrl", "https://api.deepseek.com/chat/completions");
        setField("apiKey", "test-key");
        setField("model", "deepseek-v4-pro");
        setField("temperature", 0.3);
    }

    @Test
    void generatesValidDeepSeekRequest() {
        String fakeResponse = "{\"choices\":[{\"message\":{\"content\":\"console.log('test');\"}}]}";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok(fakeResponse));

        String result = aiService.generatePlaywrightTest(sampleBug);

        assertNotNull(result);
        assertTrue(result.contains("console.log"));
    }

    @Test
    void stripsMarkdownCodeFences() {
        String responseWithFences =
                "{\"choices\":[{\"message\":{\"content\":\"```javascript\\nconsole.log('hello');\\n```\"}}]}";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok(responseWithFences));

        String result = aiService.generatePlaywrightTest(sampleBug);

        assertFalse(result.contains("```"));
        assertFalse(result.contains("javascript"));
        assertEquals("console.log('hello');", result);
    }

    @Test
    void throwsExceptionOnApiError() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection refused"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> aiService.generatePlaywrightTest(sampleBug));

        assertTrue(ex.getMessage().contains("AI generation failed"));
    }

    @Test
    void throwsExceptionOnEmptyChoices() {
        String badJson = "{\"choices\":[]}";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok(badJson));

        assertThrows(RuntimeException.class,
                () -> aiService.generatePlaywrightTest(sampleBug));
    }

    private void setField(String name, Object value) throws Exception {
        var field = AiService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(aiService, value);
    }
}
