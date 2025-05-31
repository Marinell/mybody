package com.professionisti.sport.llm;

import java.util.List;

public class MatchingResult {
    public static class ProfessionalMatch {
        public Long professionalId;
        public int rank;
        public String justification;
        public double compatibilityScore;
    }
    public List<ProfessionalMatch> matches;
    public String matchingCriteriaExplanation;
}
