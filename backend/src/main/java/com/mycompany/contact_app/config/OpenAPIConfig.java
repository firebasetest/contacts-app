package com.mycompany.contact_app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Contacts Management API")
                                                .version("1.0.0")
                                                .description("Complete API documentation for Contacts Management Application")
                                                .contact(new Contact()
                                                                .name("API Support")
                                                                .email("support@contactsmanager.com")
                                                                .url("https://github.com/firebasetest/contacts-app"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                                .components(new io.swagger.v3.oas.models.Components()
                                                .addSecuritySchemes("bearer-jwt",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("JWT Bearer token for API authentication")));
        }
}
