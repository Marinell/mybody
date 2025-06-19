package com.fitconnect.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitconnect.dto.LLMStructuredMatchResponse;
import com.fitconnect.dto.MatchResponseDTO;
import com.fitconnect.dto.MatchedProfessionalDTO;
import com.fitconnect.entity.Professional;
import com.fitconnect.entity.ProfileStatus;
import com.fitconnect.entity.ServiceRequest;
// import com.fitconnect.entity.Skill; // Skill objects are not directly fetched, skillNames from Professional used
import com.fitconnect.llm.ProfessionalMatcherAiService;
import com.fitconnect.repository.ProfessionalRepository; // Added
import com.fitconnect.repository.ServiceRequestRepository; // Added

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
// import jakarta.transaction.Transactional; // Removed
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional; // Added
import java.util.concurrent.ExecutionException; // Added
import java.util.stream.Collectors;

@ApplicationScoped
public class MatchingService {

    private static final Logger LOG = Logger.getLogger(MatchingService.class);

    @Inject
    ProfessionalMatcherAiService matcherAiService;

    @Inject
    ObjectMapper objectMapper; // Still needed if LLM response parsing uses it internally, or for other DTOs

    @Inject
    ServiceRequestRepository serviceRequestRepository; // Added

    @Inject
    ProfessionalRepository professionalRepository; // Added

    @Inject
    @ConfigProperty(name = "quarkus.langchain4j.openai.api-key")
    String openaiApiKey;

    public MatchResponseDTO findMatchesForServiceRequest(String serviceRequestId) throws ExecutionException, InterruptedException { // Removed @Transactional, Long to String, added throws
        if (openaiApiKey == null || openaiApiKey.isEmpty() || "YOUR_OPENAI_API_KEY".equals(openaiApiKey.trim())) {
            LOG.warn("OpenAI API key is not configured. Skipping LLM matching.");
            return new MatchResponseDTO("LLM matching skipped: API key not configured.", new ArrayList<>());
        }

        Optional<ServiceRequest> srOptional = serviceRequestRepository.findById(serviceRequestId);
        if (srOptional.isEmpty()) {
            LOG.warnf("ServiceRequest with ID %s not found.", serviceRequestId);
            throw new NotFoundException("ServiceRequest not found with ID: " + serviceRequestId);
        }
        ServiceRequest serviceRequest = srOptional.get();

        List<Professional> allProfessionals = professionalRepository.findAll();
        List<Professional> verifiedProfessionals = allProfessionals.stream()
            .filter(p -> p.getProfileStatus() == ProfileStatus.VERIFIED)
            .collect(Collectors.toList());

        if (verifiedProfessionals.isEmpty()) {
            LOG.info("No verified professionals available to match for service request ID: " + serviceRequestId);
            return new MatchResponseDTO("No verified professionals available.", new ArrayList<>());
        }

        String professionalProfilesData = verifiedProfessionals.stream()
            .map(pro -> String.format(
                "ID: %s, Name: %s, Profession: %s, YearsExp: %s, Summary: %s, About: %s, Skills: [%s]", // pro.id changed to pro.getId()
                pro.getId(), // Changed from pro.id
                pro.getName(),
                pro.profession,
                pro.yearsOfExperience != null ? pro.yearsOfExperience.toString() : "N/A",
                pro.summarizedSkills != null ? pro.summarizedSkills : "N/A",
                pro.aboutYou != null ? pro.aboutYou : "N/A",
                pro.getSkillNames() != null ? String.join(", ", pro.getSkillNames()) : "N/A" // Changed from pro.skills.stream().map(Skill::getName)
            ))
            .collect(Collectors.joining("\n---\n"));

        LOG.infof("Finding matches for Service Request ID: %s. Number of professionals considered: %d", serviceRequestId, verifiedProfessionals.size());
        LLMStructuredMatchResponse llmResponse = null;
        try {
            llmResponse = matcherAiService.findTopMatches(serviceRequest, professionalProfilesData);
        } catch (Exception e) {
            LOG.error("Error calling LLM for matching: " + e.getMessage(), e);
            // Consider specific exception handling or rethrowing if appropriate
            return new MatchResponseDTO("Error during LLM matching process.", new ArrayList<>());
        }

        if (llmResponse == null || llmResponse.getRankedProfessionals() == null || llmResponse.getRankedProfessionals().isEmpty()) {
            LOG.info("LLM returned no matches for service request ID: " + serviceRequestId);
            return new MatchResponseDTO(llmResponse != null && llmResponse.getRankingRationale() != null ? llmResponse.getRankingRationale() : "LLM provided no suitable matches.", new ArrayList<>());
        }

        List<MatchedProfessionalDTO> matchedDtos = new ArrayList<>();
        for (LLMStructuredMatchResponse.RankedProfessional rankedPro : llmResponse.getRankedProfessionals()) {
            if (rankedPro == null || rankedPro.getProfessionalId() == null) continue; // Ensure ID is not null

            // LLM typically returns ID as string if trained that way, or it might be Long. Adapt as necessary.
            // Assuming rankedPro.professionalId is already a String matching Firestore document ID.
            // If it's Long from LLM, it needs conversion: String.valueOf(rankedPro.getProfessionalId())
            String professionalIdStr = rankedPro.getProfessionalId(); // Assuming it's String
            Optional<Professional> proOptional = professionalRepository.findById(professionalIdStr);

            if (proOptional.isPresent()) {
                matchedDtos.add(new MatchedProfessionalDTO(proOptional.get()));
            } else {
                LOG.warnf("LLM returned Professional ID %s but it was not found in database.", professionalIdStr);
            }
        }
        // Sort DTOs by rank if not already sorted or if LLM response order isn't guaranteed
        matchedDtos.sort(Comparator.comparingInt(dto -> {
            // Find the original rank from llmResponse for this DTO's professional ID
            return llmResponse.getRankedProfessionals().stream()
                .filter(rp -> rp.getProfessionalId().equals(dto.getId())) // Assuming MatchedProfessionalDTO has getId() for professional ID
                .findFirst()
                .map(LLMStructuredMatchResponse.RankedProfessional::getRank)
                .orElse(Integer.MAX_VALUE); // Should not happen if list is consistent
        }));

        return new MatchResponseDTO(llmResponse.getRankingRationale(), matchedDtos);
    }
}
