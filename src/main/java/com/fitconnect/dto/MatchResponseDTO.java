package com.fitconnect.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDTO {
    public String rankingCriteria;
    public List<MatchedProfessionalDTO> matchedProfessionals;
}
