package com.company.pet_sitter_server.user.entity;

import com.company.pet_sitter_server.address.entity.Address;
import com.company.pet_sitter_server.enums.SitterStatus;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "sitter_profiles")
public class SitterProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String bio;
    private Double pricePerHour;
    private String experience;
    private Integer experienceYears;
    private String tradeName;
    private String petTypes;
    private String placeDescription;
    private String phone;
    private String idNumber;
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "text")
    private SitterStatus status = SitterStatus.WAITING_FOR_APPROVE;

    @Column(name = "is_approved")
    private Boolean isApproved = false;

    private Double ratingAvg = 0.0;
    private String rejectReason;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    public SitterProfile() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getBio() { return bio; }
    public Double getPricePerHour() { return pricePerHour; }
    public String getExperience() { return experience; }
    public Integer getExperienceYears() { return experienceYears; }
    public String getTradeName() { return tradeName; }
    public String getPetTypes() { return petTypes; }
    public String getPlaceDescription() { return placeDescription; }
    public String getPhone() { return phone; }
    public String getIdNumber() { return idNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public SitterStatus getStatus() { return status; }
    public Boolean getIsApproved() { return isApproved; }
    public Double getRatingAvg() { return ratingAvg; }
    public String getRejectReason() { return rejectReason; }
    public Address getAddress() { return address; }

    public void setUser(User user) { this.user = user; }
    public void setBio(String bio) { this.bio = bio; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }
    public void setExperience(String experience) { this.experience = experience; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public void setPetTypes(String petTypes) { this.petTypes = petTypes; }
    public void setPlaceDescription(String placeDescription) { this.placeDescription = placeDescription; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setStatus(SitterStatus status) {
        this.status = status;
        this.isApproved = (status == SitterStatus.APPROVED);
    }
    public void setRatingAvg(Double ratingAvg) { this.ratingAvg = ratingAvg; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public void setAddress(Address address) { this.address = address; }
}