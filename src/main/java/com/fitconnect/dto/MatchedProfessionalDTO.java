package com.fitconnect.dto;

import com.fitconnect.entity.Professional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatchedProfessionalDTO {
    public Long id;
    public String name;
    public String profession;
    public Integer yearsOfExperience;
    public String aboutYouSummary;
    public String summarizedSkills;
    public List<String> skills;

    public MatchedProfessionalDTO(Professional pro) {
        this.id = pro.id;
        this.name = pro.getName();
        this.profession = pro.profession;
        this.yearsOfExperience = pro.yearsOfExperience;
        this.aboutYouSummary = pro.aboutYou != null && pro.aboutYou.length() > 150 ? pro.aboutYou.substring(0, 150) + "..." : pro.aboutYou;
        this.summarizedSkills = pro.summarizedSkills;
        if (pro.skills != null) {
            this.skills = pro.skills.stream().map(skill -> skill.name).collect(Collectors.toList());
        }
    }
}
