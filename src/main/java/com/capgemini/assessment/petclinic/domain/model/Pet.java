package com.capgemini.assessment.petclinic.domain.model;

import com.capgemini.assessment.petclinic.common.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a companion animal registered under an Owner in PetClinic.
 */
@Entity
@Table(name = "pets")
public class Pet extends BaseEntity {

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_type", nullable = false, length = 30)
    private PetType petType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Visit> visits = new ArrayList<>();

    /**
     * Default no-args constructor required by JPA.
     */
    public Pet() {
        // Required by JPA
    }

    /**
     * Constructs a Pet entity associated with an Owner.
     *
     * @param name pet's name
     * @param birthDate pet's date of birth
     * @param petType species classification
     * @param owner owning Owner aggregate
     */
    public Pet(String name, LocalDate birthDate, PetType petType, Owner owner) {
        this.name = name;
        this.birthDate = birthDate;
        this.petType = petType;
        this.owner = owner;
    }

    /**
     * Retrieves the pet's name.
     *
     * @return pet name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the pet's name.
     *
     * @param name pet name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the pet's date of birth.
     *
     * @return birth date
     */
    public LocalDate getBirthDate() {
        return birthDate;
    }

    /**
     * Sets the pet's date of birth.
     *
     * @param birthDate birth date to set
     */
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * Retrieves the pet's species type.
     *
     * @return PetType enum value
     */
    public PetType getPetType() {
        return petType;
    }

    /**
     * Sets the pet's species type.
     *
     * @param petType PetType enum value to set
     */
    public void setPetType(PetType petType) {
        this.petType = petType;
    }

    /**
     * Retrieves the owner of this pet.
     *
     * @return associated Owner entity
     */
    public Owner getOwner() {
        return owner;
    }

    /**
     * Sets the owner of this pet.
     *
     * @param owner Owner entity to associate
     */
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    /**
     * Retrieves the clinical visits recorded for this pet.
     *
     * @return list of Visit entities
     */
    public List<Visit> getVisits() {
        return visits;
    }

    /**
     * Sets the clinical visits recorded for this pet.
     *
     * @param visits list of Visit entities
     */
    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }
}
