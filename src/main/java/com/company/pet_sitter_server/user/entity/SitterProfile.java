package com.company.pet_sitter_server.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sitter_profiles")
public class SitterProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bio;
    private Double pricePerHour;
    private String experience;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public SitterProfile() {}

    public Long getId() { return id; }
    public String getBio() { return bio; }
    public Double getPricePerHour() { return pricePerHour; }
    public String getExperience() { return experience; }
    public User getUser() { return user; }

    public void setBio(String bio) { this.bio = bio; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }
    public void setExperience(String experience) { this.experience = experience; }
    public void setUser(User user) { this.user = user; }
}