package com.mycompany.contactmgr.config;

import com.mycompany.contactmgr.service.IdentityProvisioningService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Activates @PreAuthorize expressions across your services
public class SecurityConfig {

        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
        private String externalIssuerUri;

        private final JwtTokenProvider jwtTokenProvider;
        private final IdentityProvisioningService provisioningService;

        // Injecting the service to safely supply it to our custom converter
        public SecurityConfig(IdentityProvisioningService provisioningService, JwtTokenProvider jwtTokenProvider) {
                this.provisioningService = provisioningService;
                this.jwtTokenProvider = jwtTokenProvider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // 1. Disable CSRF protection since REST APIs using JWTs are stateless
                                .csrf(csrf -> csrf.disable())

                                // 2. Enforce strict stateless session management
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // 3. Define path-level access rules
                                .authorizeHttpRequests(auth -> auth
                                                // Allow access to health checks, API documentation, or explicit public
                                                // routes
                                                .requestMatchers("/api/v1/contacts/public/**", "/actuator/health",
                                                                "/api/v1/auth/login")
                                                .permitAll()
                                                // Require valid authentication tokens for all other application routes
                                                .anyRequest().authenticated())

                                // 4. Configure the application to behave as an OAuth2 Resource Server
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                new CustomJwtAuthenticationConverter(
                                                                                                provisioningService))));

                return http.build();
        }

        @Bean
        public JwtDecoder hybridJwtDecoder() {
                // Internal HMAC Decoder
                NimbusJwtDecoder internalDecoder = NimbusJwtDecoder
                                .withSecretKey(jwtTokenProvider.getSecretKey())
                                .build();

                // External Auth Server Decoder (Keycloak, Auth0, etc.)
                JwtDecoder externalDecoder = JwtDecoders.fromIssuerLocation(externalIssuerUri);

                // Composite/Fallback Decoder Logic
                return token -> {
                        try {
                                return internalDecoder.decode(token);
                        } catch (JwtException internalEx) {
                                try {
                                        return externalDecoder.decode(token);
                                } catch (JwtException externalEx) {
                                        throw new BadJwtException(
                                                        "Token validation failed for both internal and external issuers.");
                                }
                        }
                };
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}