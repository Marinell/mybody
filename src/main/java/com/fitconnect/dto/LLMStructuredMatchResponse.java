package com.fitconnect.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LLMStructuredMatchResponse {
    public String rankingRationale;
    public List<RankedProfessional> rankedProfessionals;

    @Data
    @NoArgsConstructor
    public static class RankedProfessional {
        public Long professionalId;
        public String individualRationale;
        public int rank;
    }
}
