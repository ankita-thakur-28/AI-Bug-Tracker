package com.codewithankita.aibugtracker.unitTest;
import com.codewithankita.aibugtracker.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret",
                "aibt_super_secret_key_change_in_production_min_256bits");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 86400000L);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        String token = jwtUtil.generateToken("test@test.com", "TESTER");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken("test@test.com", "TESTER");
        assertEquals("test@test.com", jwtUtil.extractEmail(token));
    }

    @Test
    void extractRole_shouldReturnCorrectRole() {
        String token = jwtUtil.generateToken("test@test.com", "TESTER");
        assertEquals("TESTER", jwtUtil.extractRole(token));
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken("test@test.com", "ADMIN");
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalseForTamperedToken() {
        String token = jwtUtil.generateToken("test@test.com", "ADMIN");
        String tampered = token + "corrupted";
        assertFalse(jwtUtil.isTokenValid(tampered));
    }

    @Test
    void isTokenValid_shouldReturnFalseForEmptyToken() {
        assertFalse(jwtUtil.isTokenValid(""));
    }
}