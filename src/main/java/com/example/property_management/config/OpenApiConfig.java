package com.example.property_management.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Property Management API",
                version = "1.0.0",
                description = "REST API for managing users and properties.",
                contact = @Contact(
                        name = "Property Management Team",
                        email = "support@example.com"
                ),
                license = @License(
                        name = "Apache 2.0"
                )
        )
)
public class OpenApiConfig {}