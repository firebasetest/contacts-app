package com.mycompany.contact_app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mycompany.contact_app.filter.TenantContextFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Configuration
public class EmergencySecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(EmergencySecurityConfig.class);

    @Value("${app.security.break-glass-token-hash}")
    private String breakGlassTokenHash;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Bean
    @Order(1) // High priority: Evaluated BEFORE the default OAuth2 resource server chain
    public SecurityFilterChain emergencyFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/emergency/**") // Only guard endpoints matching this pattern
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())
                // Inject our localized token validation verification hook
                .addFilterBefore(new EmergencyAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Internal filter dedicated to parsing and raising system metrics for emergency
     * actions.
     */
    private class EmergencyAuthenticationFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                @NonNull FilterChain filterChain)
                throws ServletException, IOException {

            String tokenHeader = request.getHeader("X-Emergency-Break-Glass-Token");
            String targetTenantHeader = request.getHeader("X-BU-ID");

            if (tokenHeader == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Missing Break-Glass credentials.");
                return;
            }

            // Verify incoming token against the secure environment cryptographic hash
            if (passwordEncoder.matches(tokenHeader, breakGlassTokenHash)) {

                // CRITICAL ALIGNMENT: Trigger an instant systemic log alert
                log.error(
                        "!!! CRITICAL SECURITY ALERT: Break-Glass Emergency Authentication Pattern initiated via IP: {} for Target Tenant [X-BU-ID]: {} !!!",
                        request.getRemoteAddr(), targetTenantHeader);

                // Set up an isolated system security principal completely separated from
                // standard User DB tables
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "SYSTEM_EMERGENCY_ADMIN",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_EMERGENCY_ADMIN")));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Populate Multi-Tenant context manually to satisfy Postgres RLS matching
                // requirements
                if (targetTenantHeader != null && !targetTenantHeader.isBlank()) {
                    TenantContextFilter.CURRENT_TENANT.set(targetTenantHeader);
                }

                try {
                    filterChain.doFilter(request, response);
                } finally {
                    // Always clear context boundaries at thread termination
                    TenantContextFilter.CURRENT_TENANT.remove();
                }
            } else {
                log.warn("WARNING: Unauthorized attempt to access Break-Glass Emergency endpoints from IP: {}",
                        request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid Emergency Credentials.");
            }
        }
    }
}