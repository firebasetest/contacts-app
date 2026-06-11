package com.mycompany.contact_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.mycompany.contact_app.filter.TenantContextFilter;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // In a real app, integrate with Spring Security to get the logged-in User ID
        return () -> Optional.ofNullable(TenantContextFilter.CURRENT_TENANT.get());
    }
}
