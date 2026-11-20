package com.capgemini.assessment.petclinic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures OpenAPI 3.1 / Swagger UI documentation metadata for the PetClinic REST and Outbox Integration APIs.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Default constructor for OpenApiConfig.
     */
    public OpenApiConfig() {
        // Default constructor
    }

    /**
     * Builds the OpenAPI 3.1 specification bean rendered by Swagger UI.
     *
     * @return configured OpenAPI specification instance
     */
    @Bean
    public OpenAPI petClinicOpenAPI() {
        Contact contact = new Contact()
                .name("Amar Pardeshi — Capgemini Backend Engineering Assessment")
                .url("https://github.com/spring-petclinic/spring-petclinic-rest");
        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");
        Info info = new Info()
                .title("Spring PetClinic REST & Reliable Outbox Integration API (Spring Boot 4.x)")
                .version("1.0.0")
                .description("Spec-Driven Development implementation of Spring PetClinic REST with Transactional Outbox, "
                        + "Exponential Backoff Webhook Relay, Dead-Letter Recovery, and Idempotent Consumer Simulation.")
                .contact(contact)
                .license(license);
        return new OpenAPI().info(info);
    }
}
