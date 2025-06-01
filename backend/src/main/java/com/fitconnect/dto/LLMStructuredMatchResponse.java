package com.fitconnect.dto;

import java.util.List;

public class LLMStructuredMatchResponse {
    public String rankingRationale;
    public List<RankedProfessional> rankedProfessionals;

    public static class RankedProfessional {
        public Long professionalId;
        public String individualRationale;
        public int rank;

        // Getters & Setters for Jackson
        public Long getProfessionalId() { return professionalId; }
        public void setProfessionalId(Long professionalId) { this.professionalId = professionalId; }
        public String getIndividualRationale() { return individualRationale; }
        public void setIndividualRationale(String individualRationale) { this.individualRationale = individualRationale; }
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
    }
    public String getRankingRationale() { return rankingRationale; }
    public void setRankingRationale(String rankingRationale) { this.rankingRationale = rankingRationale; }
    public List<RankedProfessional> getRankedProfessionals() { return rankedProfessionals; }
    public void setRankedProfessionals(List<RankedProfessional> rankedProfessionals) { this.rankedProfessionals = rankedProfessionals; }
}
