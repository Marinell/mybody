package com.professionisti.sport.model;

import com.professionisti.sport.model.enums.ScreeningStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "professionals")
public class Professional extends User {

    private String specialization;
    private Integer experienceYears;
    private String phoneNumber;
    private String blogUrl;
    private String socialMediaLink;

    @Enumerated(EnumType.STRING)
    private ScreeningStatus screeningStatus;

    @Lob // For potentially long text
    @Column(columnDefinition = "TEXT")
    private String skillsSummary; // To be populated by LLM

    @OneToMany(mappedBy = "professional", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    // Could also have a list of service requests they are matched/assigned to
    // @ManyToMany(mappedBy = "matchedProfessionals")
    // private List<ServiceRequest> matchedRequests = new ArrayList<>();


    // Getters and Setters
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getBlogUrl() { return blogUrl; }
    public void setBlogUrl(String blogUrl) { this.blogUrl = blogUrl; }
    public String getSocialMediaLink() { return socialMediaLink; }
    public void setSocialMediaLink(String socialMediaLink) { this.socialMediaLink = socialMediaLink; }
    public ScreeningStatus getScreeningStatus() { return screeningStatus; }
    public void setScreeningStatus(ScreeningStatus screeningStatus) { this.screeningStatus = screeningStatus; }
    public String getSkillsSummary() { return skillsSummary; }
    public void setSkillsSummary(String skillsSummary) { this.skillsSummary = skillsSummary; }
    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }
}
