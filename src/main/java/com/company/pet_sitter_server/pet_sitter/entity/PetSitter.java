package com.company.pet_sitter_server.pet_sitter.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pet_sitters")
public class PetSitter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String fullName;
    private String phone;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String intro;

    private String tradeName;
    private String petTypes;

    @Column(columnDefinition = "TEXT")
    private String services;

    @Column(columnDefinition = "TEXT")
    private String place;

    private String experience;
    private Double pricePerHour;

    // Address
    @Column(columnDefinition = "TEXT")
    private String address;
    private String district;
    private String subDistrict;
    private String province;
    private String postCode;

    // Location
    private Double latitude;
    private Double longitude;

    // Images
    private String profileImage;

    @ElementCollection
    @CollectionTable(name = "pet_sitter_gallery", joinColumns = @JoinColumn(name = "pet_sitter_id"))
    @Column(name = "image_url")
    private List<String> gallery = new ArrayList<>();

    // Status
    private String status = "pending";
    private String rejectionMessage;

    // ===== GETTERS =====
    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getIntro() { return intro; }
    public String getTradeName() { return tradeName; }
    public String getPetTypes() { return petTypes; }
    public String getServices() { return services; }
    public String getPlace() { return place; }
    public String getExperience() { return experience; }
    public Double getPricePerHour() { return pricePerHour; }
    public String getAddress() { return address; }
    public String getDistrict() { return district; }
    public String getSubDistrict() { return subDistrict; }
    public String getProvince() { return province; }
    public String getPostCode() { return postCode; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getProfileImage() { return profileImage; }
    public List<String> getGallery() { return gallery; }
    public String getStatus() { return status; }
    public String getRejectionMessage() { return rejectionMessage; }

    // ===== SETTERS =====
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setIntro(String intro) { this.intro = intro; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public void setPetTypes(String petTypes) { this.petTypes = petTypes; }
    public void setServices(String services) { this.services = services; }
    public void setPlace(String place) { this.place = place; }
    public void setExperience(String experience) { this.experience = experience; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }
    public void setAddress(String address) { this.address = address; }
    public void setDistrict(String district) { this.district = district; }
    public void setSubDistrict(String subDistrict) { this.subDistrict = subDistrict; }
    public void setProvince(String province) { this.province = province; }
    public void setPostCode(String postCode) { this.postCode = postCode; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public void setGallery(List<String> gallery) { this.gallery = gallery; }
    public void setStatus(String status) { this.status = status; }
    public void setRejectionMessage(String rejectionMessage) { this.rejectionMessage = rejectionMessage; }
}
