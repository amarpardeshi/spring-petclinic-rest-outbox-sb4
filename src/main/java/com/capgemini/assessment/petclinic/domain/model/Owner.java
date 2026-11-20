package com.capgemini.assessment.petclinic.domain.model;

import com.capgemini.assessment.petclinic.common.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a pet owner registered with the veterinary clinic.
 */
@Entity
@Table(name = "owners")
public class Owner extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "city", nullable = false, length = 80)
    private String city;

    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pet> pets = new ArrayList<>();

    /**
     * Default no-args constructor required by JPA.
     */
    public Owner() {
        // Required by JPA
    }

    /**
     * Constructs an Owner with full demographic and contact attributes.
     *
     * @param firstName owner's given name
     * @param lastName owner's family name
     * @param address street address
     * @param city city name
     * @param telephone contact telephone number
     */
    public Owner(String firstName, String lastName, String address, String city, String telephone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.telephone = telephone;
    }

    /**
     * Retrieves the owner's first name.
     *
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the owner's first name.
     *
     * @param firstName first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Retrieves the owner's last name.
     *
     * @return last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the owner's last name.
     *
     * @param lastName last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Retrieves the owner's street address.
     *
     * @return street address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the owner's street address.
     *
     * @param address street address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Retrieves the owner's city.
     *
     * @return city name
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the owner's city.
     *
     * @param city city name to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Retrieves the owner's telephone number.
     *
     * @return telephone number
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * Sets the owner's telephone number.
     *
     * @param telephone telephone number to set
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * Retrieves the list of pets belonging to this owner.
     *
     * @return list of Pet entities
     */
    public List<Pet> getPets() {
        return pets;
    }

    /**
     * Sets the list of pets belonging to this owner.
     *
     * @param pets list of Pet entities to associate
     */
    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }
}
