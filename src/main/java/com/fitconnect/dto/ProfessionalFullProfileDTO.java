package com.fitconnect.dto;

import com.fitconnect.entity.Professional;
import com.fitconnect.entity.ProfessionalDocument;
import com.fitconnect.entity.ProfileStatus;
import com.fitconnect.entity.Skill;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfessionalFullProfileDTO {
    public Long id;
    public String name;
    public String email;
    public String phoneNumber;
    public String profession;
    public String address;
    public String postalCode;
    public Integer yearsOfExperience;
    public String qualifications;
    public String aboutYou;
    public Map<String, String> socialMediaLinks;
    public ProfileStatus profileStatus;
    public String summarizedSkills;
    public List<String> skills; // Names of skills
    public List<DocumentInfoDTO> documents;

    public static class DocumentInfoDTO {
        public Long id;
        public String fileName;
        public String fileType;
        // Not exposing storagePath to the professional directly unless needed

        public DocumentInfoDTO(ProfessionalDocument doc) {
            this.id = doc.id;
            this.fileName = doc.fileName;
            this.fileType = doc.fileType;
        }
    }

    public ProfessionalFullProfileDTO(Professional pro) {
        this.id = pro.id;
        this.name = pro.getName();
        this.email = pro.getEmail();
        this.phoneNumber = pro.getPhoneNumber();
        this.profession = pro.profession;
        this.address = pro.address;
        this.postalCode = pro.postalCode;
        this.yearsOfExperience = pro.yearsOfExperience;
        this.qualifications = pro.qualifications;
        this.aboutYou = pro.aboutYou;
        this.socialMediaLinks = pro.socialMediaLinks;
        this.profileStatus = pro.profileStatus;
        this.summarizedSkills = pro.summarizedSkills;
        if (pro.skills != null) {
            this.skills = pro.skills.stream().map(Skill::getName).collect(Collectors.toList());
        }
        if (pro.documents != null) {
            this.documents = pro.documents.stream().map(DocumentInfoDTO::new).collect(Collectors.toList());
        }
    }
}
