package com.fitconnect.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "professionals")
@Getter
@Setter
public class Professional extends User {

    public String profession;
    public String address;
    public String postalCode;
    public Integer yearsOfExperience;

    @Column(columnDefinition = "TEXT")
    public String qualifications; // As per HTML, this is a textarea

    @Column(columnDefinition = "TEXT")
    public String aboutYou; // As per HTML, this is a textarea

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "professional_links", joinColumns = @JoinColumn(name = "professional_id"))
    @MapKeyColumn(name = "link_type") // e.g., "LINKEDIN", "WEBSITE"
    @Column(name = "url")
    public Map<String, String> socialMediaLinks; // Store links like LinkedIn, Website, etc.

    @Enumerated(EnumType.STRING)
    public ProfileStatus profileStatus;

    @Column(columnDefinition = "TEXT")
    public String summarizedSkills; // To be populated by LLM

    @OneToMany(mappedBy = "professional", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<ProfessionalDocument> documents;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "professional_skills",
        joinColumns = @JoinColumn(name = "professional_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    public List<Skill> skills;

    @OneToMany(mappedBy = "professional")
    public List<Appointment> appointments;

    public Professional() {
        this.role = UserRole.PROFESSIONAL;
        this.profileStatus = ProfileStatus.PENDING_VERIFICATION;
    }

    // Getters and setters for new fields
    // ... (Consider adding if direct field access is not preferred, Panache usually allows direct access)
}
