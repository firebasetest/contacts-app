package com.mycompany.contact_app.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.mockito.Mockito;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        // Returns a mocked decoder to bypass remote network JWK Set lookups during
        // testing
        return Mockito.mock(JwtDecoder.class);
    }
}