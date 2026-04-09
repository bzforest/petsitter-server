package com.company.pet_sitter_server.user.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "user_profiles")

public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("full_name")
    private String fullName;

    private String phone;

    @Column(columnDefinition = "text")
    private String address;

    @JsonProperty("profile_image")
    @Column(columnDefinition = "text")
    private String profileImage;

    private Double latitude;
    private Double longitude;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @JsonProperty("id_number")
    private String idNumber;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public UserProfile() {}

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getProfileImage() { return profileImage; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getIdNumber() { return idNumber; }
    public User getUser() { return user; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public void setUser(User user) { this.user = user; }
}
