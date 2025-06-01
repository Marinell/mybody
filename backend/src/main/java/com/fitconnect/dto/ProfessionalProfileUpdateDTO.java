package com.fitconnect.dto;

import java.util.Map;

// Contains fields a professional can update for their own profile.
public class ProfessionalProfileUpdateDTO {
    // Not all fields are updatable directly, e.g., email (usually part of auth identity), status.
    // Name and phone number might be updatable via a general User update DTO if separated.
    // For now, including some common ones here.
    private String name; // Assuming name can be updated
    private String phoneNumber;
    private String profession;
    private String address;
    private String postalCode;
    private Integer yearsOfExperience;
    private String qualifications; // If updated, might require re-verification
    private String aboutYou;
    private Map<String, String> socialMediaLinks;
    // Password update should be a separate endpoint/flow.
    // Document updates (add/delete) would also be separate if complex.

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
    public String getQualifications() { return qualifications; }
    public void setQualifications(String qualifications) { this.qualifications = qualifications; }
    public String getAboutYou() { return aboutYou; }
    public void setAboutYou(String aboutYou) { this.aboutYou = aboutYou; }
    public Map<String, String> getSocialMediaLinks() { return socialMediaLinks; }
    public void setSocialMediaLinks(Map<String, String> socialMediaLinks) { this.socialMediaLinks = socialMediaLinks; }
}
