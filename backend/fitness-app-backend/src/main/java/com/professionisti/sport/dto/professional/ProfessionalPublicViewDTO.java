package com.professionisti.sport.dto.professional;

import com.professionisti.sport.model.Professional;
import com.professionisti.sport.model.enums.ScreeningStatus; // Just for safety check in factory

public class ProfessionalPublicViewDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String specialization;
    private Integer experienceYears;
    private String skillsSummary;
    private String email; // Contact info
    private String phoneNumber; // Contact info
    private String blogUrl;
    private String socialMediaLink;
    // Documents are not typically public

    public ProfessionalPublicViewDTO(Long id, String firstName, String lastName, String specialization,
                                     Integer experienceYears, String skillsSummary, String email,
                                     String phoneNumber, String blogUrl, String socialMediaLink) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.experienceYears = experienceYears;
        this.skillsSummary = skillsSummary;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.blogUrl = blogUrl;
        this.socialMediaLink = socialMediaLink;
    }

    public static ProfessionalPublicViewDTO fromEntity(Professional entity) {
        if (entity == null || entity.getScreeningStatus() != ScreeningStatus.APPROVED) {
            return null; // Or throw an exception if called inappropriately
        }
        return new ProfessionalPublicViewDTO(
            entity.getId(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getSpecialization(),
            entity.getExperienceYears(),
            entity.getSkillsSummary(),
            entity.getEmail(), // Exposing email
            entity.getPhoneNumber(), // Exposing phone number
            entity.getBlogUrl(),
            entity.getSocialMediaLink()
        );
    }

    // Getters
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSpecialization() { return specialization; }
    public Integer getExperienceYears() { return experienceYears; }
    public String getSkillsSummary() { return skillsSummary; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getBlogUrl() { return blogUrl; }
    public String getSocialMediaLink() { return socialMediaLink; }
}
