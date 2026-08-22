package com.mycompany.contact_app.config;

import com.mycompany.contact_app.service.IdentityProvisioningService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Activates @PreAuthorize expressions across your services
public class SecurityConfig {

        private final IdentityProvisioningService provisioningService;

        // Injecting the service to safely supply it to our custom converter
        public SecurityConfig(IdentityProvisioningService provisioningService) {
                this.provisioningService = provisioningService;
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
                                                .requestMatchers("/api/v1/contacts/public/**", "/actuator/health")
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
}