package com.company.pet_sitter_server.dto.pet;

public class PetRequest {
    private Long userId;
    private String name;
    private String type;
    private String breed;
    private String sex;
    private Integer age;
    private Double weight;
    private String aboutPet;
    private String imageUrl;

    public PetRequest() {
    }

    public PetRequest(Long userId, String name, String type, String breed, String sex, Integer age, Double weight,
            String aboutPet, String imageUrl) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.sex = sex;
        this.age = age;
        this.weight = weight;
        this.aboutPet = aboutPet;
        this.imageUrl = imageUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getAboutPet() {
        return aboutPet;
    }

    public void setAboutPet(String aboutPet) {
        this.aboutPet = aboutPet;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "PetRequest [userId=" + userId + ", name=" + name + ", type=" + type + ", breed=" + breed + ", sex="
                + sex + ", age=" + age + ", weight=" + weight + ", aboutPet=" + aboutPet + ", imageUrl=" + imageUrl
                + "]";
    }
}
