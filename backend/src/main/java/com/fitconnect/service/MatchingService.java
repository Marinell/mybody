package com.fitconnect.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitconnect.dto.LLMStructuredMatchResponse;
import com.fitconnect.dto.MatchResponseDTO;
import com.fitconnect.dto.MatchedProfessionalDTO;
import com.fitconnect.entity.Professional;
import com.fitconnect.entity.ProfileStatus;
import com.fitconnect.entity.ServiceRequest;
import com.fitconnect.entity.Skill;
import com.fitconnect.llm.ProfessionalMatcherAiService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class MatchingService {

    private static final Logger LOG = Logger.getLogger(MatchingService.class);

    @Inject
    ProfessionalMatcherAiService matcherAiService;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @ConfigProperty(name = "quarkus.langchain4j.openai.api-key")
    String openaiApiKey;

    @Transactional
    public MatchResponseDTO findMatchesForServiceRequest(Long serviceRequestId) {
        if (openaiApiKey == null || openaiApiKey.isEmpty() || "YOUR_OPENAI_API_KEY".equals(openaiApiKey.trim())) {
            LOG.warn("OpenAI API key is not configured. Skipping LLM matching.");
            return new MatchResponseDTO("LLM matching skipped: API key not configured.", new ArrayList<>());
        }

        ServiceRequest serviceRequest = ServiceRequest.findById(serviceRequestId);
        if (serviceRequest == null) {
            LOG.warnf("ServiceRequest with ID %d not found.", serviceRequestId);
            throw new NotFoundException("ServiceRequest not found with ID: " + serviceRequestId);
        }

        List<Professional> verifiedProfessionals = Professional.list("profileStatus", ProfileStatus.VERIFIED);
        if (verifiedProfessionals.isEmpty()) {
            LOG.info("No verified professionals available to match for service request ID: " + serviceRequestId);
            return new MatchResponseDTO("No verified professionals available.", new ArrayList<>());
        }

        String professionalProfilesData = verifiedProfessionals.stream()
            .map(pro -> String.format(
                "ID: %d, Name: %s, Profession: %s, YearsExp: %s, Summary: %s, About: %s, Skills: [%s]",
                pro.id,
                pro.getName(),
                pro.profession,
                pro.yearsOfExperience != null ? pro.yearsOfExperience.toString() : "N/A",
                pro.summarizedSkills != null ? pro.summarizedSkills : "N/A",
                pro.aboutYou != null ? pro.aboutYou : "N/A",
                pro.skills != null ? pro.skills.stream().map(Skill::getName).collect(Collectors.joining(", ")) : "N/A"
            ))
            .collect(Collectors.joining("\n---\n"));

        LOG.infof("Finding matches for Service Request ID: %d. Number of professionals considered: %d", serviceRequestId, verifiedProfessionals.size());
        LLMStructuredMatchResponse llmResponse = null;
        try {
            llmResponse = matcherAiService.findTopMatches(serviceRequest, professionalProfilesData);
        } catch (Exception e) {
            LOG.error("Error calling LLM for matching: " + e.getMessage(), e);
            return new MatchResponseDTO("Error during LLM matching process.", new ArrayList<>());
        }


        if (llmResponse == null || llmResponse.getRankedProfessionals() == null || llmResponse.getRankedProfessionals().isEmpty()) {
            LOG.info("LLM returned no matches for service request ID: " + serviceRequestId);
            return new MatchResponseDTO(llmResponse != null && llmResponse.getRankingRationale() != null ? llmResponse.getRankingRationale() : "LLM provided no suitable matches.", new ArrayList<>());
        }

        List<MatchedProfessionalDTO> matchedDtos = new ArrayList<>();
        llmResponse.getRankedProfessionals().stream()
            .filter(Objects::nonNull)
            .filter(rankedPro -> rankedPro.getProfessionalId() != null) // Ensure ID is not null
            .sorted(Comparator.comparingInt(LLMStructuredMatchResponse.RankedProfessional::getRank))
            .forEach(rankedPro -> {
                Professional proEntity = Professional.findById(rankedPro.professionalId);
                if (proEntity != null) {
                    matchedDtos.add(new MatchedProfessionalDTO(proEntity));
                } else {
                    LOG.warnf("LLM returned Professional ID %d but it was not found in database.", rankedPro.professionalId);
                }
            });

        return new MatchResponseDTO(llmResponse.getRankingRationale(), matchedDtos);
    }
}
