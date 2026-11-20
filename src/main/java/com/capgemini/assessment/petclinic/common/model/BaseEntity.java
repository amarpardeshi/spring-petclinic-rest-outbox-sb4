package com.capgemini.assessment.petclinic.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;

/**
 * Mapped superclass providing identity and audit timestamp columns for all PetClinic JPA entities.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Default constructor for BaseEntity subclasses.
     */
    protected BaseEntity() {
        // Protected constructor for JPA inheritance
    }

    /**
     * Lifecycle callback initializing audit timestamps prior to database insertion.
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Lifecycle callback refreshing the update timestamp prior to database modification.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Retrieves the primary key identifier of the entity.
     *
     * @return primary key ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the primary key identifier of the entity.
     *
     * @param id primary key ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retrieves the UTC creation timestamp of the entity.
     *
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the UTC creation timestamp of the entity.
     *
     * @param createdAt creation timestamp to set
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Retrieves the UTC last-modified timestamp of the entity.
     *
     * @return last-modified timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the UTC last-modified timestamp of the entity.
     *
     * @param updatedAt last-modified timestamp to set
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
