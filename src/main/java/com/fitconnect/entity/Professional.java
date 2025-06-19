package com.fitconnect.entity;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Professional extends User {

    public String profession;
    public String address;
    public String postalCode;
    public Integer yearsOfExperience;

    public String qualifications; // As per HTML, this is a textarea

    public String aboutYou; // As per HTML, this is a textarea

    public Map<String, String> socialMediaLinks; // Store links like LinkedIn, Website, etc.

    public ProfileStatus profileStatus;

    public String summarizedSkills; // To be populated by LLM

    public List<String> professionalDocumentReferences; // Changed from List<ProfessionalDocument>

    public List<String> skillNames; // Changed from List<Skill>

    // appointments list removed, will be queried

    public Professional() {
        this.role = UserRole.PROFESSIONAL;
        this.profileStatus = ProfileStatus.PENDING_VERIFICATION;
    }

    // Getters and setters for new fields
    // ... (Lombok handles this)
}
