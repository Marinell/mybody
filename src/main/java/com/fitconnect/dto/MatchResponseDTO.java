package com.fitconnect.dto;

import java.util.List;

public class MatchResponseDTO {
    public String rankingCriteria;
    public List<MatchedProfessionalDTO> matchedProfessionals;

    public MatchResponseDTO(String rankingCriteria, List<MatchedProfessionalDTO> matchedProfessionals) {
        this.rankingCriteria = rankingCriteria;
        this.matchedProfessionals = matchedProfessionals;
    }
    public MatchResponseDTO() {}
}
