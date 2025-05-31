package com.professionisti.sport.llm;

// import dev.langchain4j.service.UserMessage;
// import dev.langchain4j.service.AiService;
// import dev.langchain4j.service.SystemMessage;

import java.util.List;

// @AiService // This annotation would activate it if LangChain4j is configured
public interface ProfessionalMatcher {

    // @SystemMessage("You are an expert matchmaking system. Given a client's service request and a list of professionals with their skills and specializations (in JSON format), " +
    //                "identify the top 3 professionals that best match the request. " +
    //                "Return a JSON string containing a list of objects, each with 'professionalId', 'rank' (1, 2, or 3), 'justification', and 'compatibilityScore' (0.0 to 1.0). " +
    //                "Also include an overall 'matchingCriteriaExplanation' field explaining the general criteria used for ranking.")
    String findMatchingProfessionals(String clientRequestDetails, String professionalsJsonList);

    // Example of a more structured return type if preferred over JSON string, but this requires DTOs.
    // MatchingResult findMatchingProfessionalsStructured(String clientRequestDetails, List<ProfessionalProfileData> professionals);
}
