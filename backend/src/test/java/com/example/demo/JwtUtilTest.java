package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User user;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        user = new User();
        user.setUsername("maya_backend");
    }

    @Test
    void generateTokenIncludesUsernameClaim() {
        String token = jwtUtil.generateToken(user);

        Claims claims = jwtUtil.parseToken(token);

        assertEquals("maya_backend", claims.get("username"));
        assertNotNull(claims.getIssuedAt());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void parseTokenThrowsErrorForInvalidToken() {
        assertThrows(JwtException.class, () -> jwtUtil.parseToken("not-a-valid-token"));
    }
}
