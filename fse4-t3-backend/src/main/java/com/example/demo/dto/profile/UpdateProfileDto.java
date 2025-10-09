package com.example.demo.dto.profile;


public class UpdateProfileDto {
    private String name;
    private String country;
    private String postalCode;

    public UpdateProfileDto() {}

    public UpdateProfileDto(String name, String country, String postalCode) {
        this.name = name;
        this.country = country;
        this.postalCode = postalCode;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
}
