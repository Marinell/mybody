package com.fitconnect.dto;

import com.fitconnect.entity.Professional;
import com.fitconnect.entity.Skill;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfessionalPublicProfileDTO {
    public Long id;
    public String name;
    public String email; // Contact email
    public String phoneNumber; // Contact phone
    public String profession;
    public Integer yearsOfExperience;
    public String qualificationsSummary; // Could be a snippet or full
    public String aboutYou;
    public String summarizedSkills;
    public List<String> skills;
    public Map<String, String> socialMediaLinks;

    public ProfessionalPublicProfileDTO(Professional pro) {
        this.id = pro.id;
        this.name = pro.getName();
        this.email = pro.getEmail(); // Exposing contact email
        this.phoneNumber = pro.getPhoneNumber(); // Exposing contact phone
        this.profession = pro.profession;
        this.yearsOfExperience = pro.yearsOfExperience;
        this.qualificationsSummary = pro.qualifications; // Decide how much to show
        this.aboutYou = pro.aboutYou;
        this.summarizedSkills = pro.summarizedSkills;
        if (pro.skills != null) {
            this.skills = pro.skills.stream().map(Skill::getName).collect(Collectors.toList());
        }
        this.socialMediaLinks = pro.socialMediaLinks;
    }
}
