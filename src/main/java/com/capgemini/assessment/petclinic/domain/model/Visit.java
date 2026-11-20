package com.capgemini.assessment.petclinic.domain.model;

import com.capgemini.assessment.petclinic.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * JPA entity representing a scheduled or completed veterinary visit for a Pet.
 */
@Entity
@Table(name = "visits")
public class Visit extends BaseEntity {

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "vet_name", nullable = false, length = 120)
    private String vetName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    /**
     * Default no-args constructor required by JPA.
     */
    public Visit() {
        // Required by JPA
    }

    /**
     * Constructs a Visit entity for a given Pet.
     *
     * @param visitDate date of the veterinary appointment
     * @param description clinical notes or reason for visit
     * @param vetName attending veterinarian's full name
     * @param pet Pet receiving care
     */
    public Visit(LocalDate visitDate, String description, String vetName, Pet pet) {
        this.visitDate = visitDate;
        this.description = description;
        this.vetName = vetName;
        this.pet = pet;
    }

    /**
     * Retrieves the date of the veterinary visit.
     *
     * @return visit date
     */
    public LocalDate getVisitDate() {
        return visitDate;
    }

    /**
     * Sets the date of the veterinary visit.
     *
     * @param visitDate visit date to set
     */
    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    /**
     * Retrieves the clinical description of the visit.
     *
     * @return visit description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the clinical description of the visit.
     *
     * @param description visit description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retrieves the attending veterinarian's name.
     *
     * @return veterinarian name
     */
    public String getVetName() {
        return vetName;
    }

    /**
     * Sets the attending veterinarian's name.
     *
     * @param vetName veterinarian name to set
     */
    public void setVetName(String vetName) {
        this.vetName = vetName;
    }

    /**
     * Retrieves the pet associated with this visit.
     *
     * @return associated Pet entity
     */
    public Pet getPet() {
        return pet;
    }

    /**
     * Sets the pet associated with this visit.
     *
     * @param pet Pet entity to associate
     */
    public void setPet(Pet pet) {
        this.pet = pet;
    }
}
