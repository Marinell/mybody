package com.fitconnect.llm;

import com.fitconnect.dto.LLMStructuredMatchResponse;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.AiService;
import dev.langchain4j.service.V;

@AiService
public interface ProfessionalMatcherAiService {

    @SystemMessage("""
        You are a sophisticated AI matching engine for a platform connecting clients with fitness, wellness, and sports professionals.
        Your goal is to find the top 3 best-matched professionals for a given client's service request.
        You will be provided with the client's service request details and a list of available, verified professionals with their profiles (including summaries, skills, experience).

        Analyze the request against each professional's data. Consider:
        1.  Relevance of skills and specializations to the client's stated needs.
        2.  Years of experience in relevant areas.
        3.  Keywords in the client's request matching the professional's profile or summarized skills.

        Output a JSON object containing:
        1.  A general "rankingRationale" explaining the key factors considered for the overall ranking (max 100 words).
        2.  A list called "rankedProfessionals" with the top 3 professionals. Each element in the list should be an object with:
            - "professionalId": The ID of the professional.
            - "rank": Their rank (1, 2, or 3).
            - "individualRationale": (Optional) A very brief (max 30 words) explanation why this specific professional is a good match for this rank.

        Example JSON output format:
        {
          "rankingRationale": "Matches are ranked based on direct skill alignment with the request, demonstrated experience in similar areas, and positive indications from their profile summaries.",
          "rankedProfessionals": [
            { "professionalId": 101, "rank": 1, "individualRationale": "Excellent match for yoga and stress reduction." },
            { "professionalId": 105, "rank": 2, "individualRationale": "Strong experience in strength training." },
            { "professionalId": 102, "rank": 3, "individualRationale": "Good alignment with general fitness goals." }
          ]
        }
        Ensure the output is valid JSON. Only include professionals from the provided list.
        If fewer than 3 professionals are suitable, return as many as are suitable. If no professionals are suitable, return an empty list for rankedProfessionals.
        """)
    @UserMessage("""
        Client's Service Request:
        Category: {{serviceRequest.category}}
        Description: {{serviceRequest.serviceDescription}}
        Budget: {{serviceRequest.budget}}

        Available Professionals (ID, Name, Profession, Years of Experience, Summarized Skills, About, Other Skills):
        ---
        {{professionalProfiles}}
        ---
        Please provide the top 3 matches in the specified JSON format.
        """)
    LLMStructuredMatchResponse findTopMatches(
        @V("serviceRequest") com.fitconnect.entity.ServiceRequest serviceRequest,
        @V("professionalProfiles") String professionalProfiles
    );
}
