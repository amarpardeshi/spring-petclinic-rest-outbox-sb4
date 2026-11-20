package com.capgemini.assessment.petclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Spring Boot 4.x PetClinic REST application with reliable Transactional Outbox integration.
 */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class PetClinicApplication {

    /**
     * Default constructor for PetClinicApplication.
     */
    public PetClinicApplication() {
        // Default constructor required by Spring Boot
    }

    /**
     * Bootstraps the Spring Boot 4.x PetClinic REST application context.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(PetClinicApplication.class, args);
    }
}
