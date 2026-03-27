package com.company.pet_sitter_server.address.entity;

import com.company.pet_sitter_server.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label; // บ้าน / ที่ทำงาน

    private String addressLine;
    private String city;
    private String province;
    private String postalCode;

    // 🔥 FK → user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Address() {}

    public Long getId() { return id; }
    public String getLabel() { return label; }
    public String getAddressLine() { return addressLine; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getPostalCode() { return postalCode; }
    public User getUser() { return user; }

    public void setLabel(String label) { this.label = label; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public void setCity(String city) { this.city = city; }
    public void setProvince(String province) { this.province = province; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public void setUser(User user) { this.user = user; }
}