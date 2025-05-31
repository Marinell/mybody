package com.professionisti.sport.service;

import com.professionisti.sport.model.Professional;
import com.professionisti.sport.model.ServiceRequest;
import com.professionisti.sport.model.User;
import com.professionisti.sport.model.enums.Role;
import com.professionisti.sport.model.enums.ScreeningStatus;
import com.professionisti.sport.model.enums.ServiceRequestStatus;
import com.professionisti.sport.dto.servicerequest.ServiceRequestViewDTO; // To return updated view
// import com.professionisti.sport.llm.ProfessionalMatcher; // Placeholder for LLM

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

// For JSON serialization if needed for LLM input
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.core.JsonProcessingException;

@ApplicationScoped
public class MatchingService {

    @Inject
    EntityManager entityManager;

    // @Inject
    // ProfessionalMatcher professionalMatcher; // Placeholder for LLM

    // private final ObjectMapper objectMapper = new ObjectMapper(); // For serializing professional list to JSON

    @Transactional
    public ServiceRequestViewDTO findAndAssignMatches(Long serviceRequestId, String clientEmail) {
        ServiceRequest serviceRequest = entityManager.find(ServiceRequest.class, serviceRequestId);
        if (serviceRequest == null) {
            throw new WebApplicationException("Service request not found: " + serviceRequestId, Response.Status.NOT_FOUND);
        }

        if (serviceRequest.getClient() == null || !serviceRequest.getClient().getEmail().equals(clientEmail)) {
             throw new WebApplicationException("Client not authorized for this service request.", Response.Status.FORBIDDEN);
        }

        if (serviceRequest.getStatus() != ServiceRequestStatus.OPEN) {
            throw new WebApplicationException("Service request is not in OPEN state, cannot process for matching.", Response.Status.CONFLICT);
        }

        List<Professional> approvedProfessionals = entityManager.createQuery(
                "SELECT p FROM Professional p WHERE p.screeningStatus = :status", Professional.class)
                .setParameter("status", ScreeningStatus.APPROVED)
                .getResultList();

        if (approvedProfessionals.isEmpty()) {
            serviceRequest.setMatchingCriteriaExplanation("No approved professionals available at the moment to perform matching.");
            serviceRequest.setStatus(ServiceRequestStatus.MATCHED);
            entityManager.merge(serviceRequest);
            return ServiceRequestViewDTO.fromEntity(serviceRequest);
        }

        String professionalsJson = approvedProfessionals.stream()
                                        .map(p -> String.format("{\"id\":%d, \"specialization\":\"%s\", \"skillsSummary\":\"%s\"}",
                                                                p.getId(),
                                                                p.getSpecialization() != null ? p.getSpecialization().replace("\"", "\\"") : "",
                                                                p.getSkillsSummary() != null ? p.getSkillsSummary().replace("\"", "\\"") : ""))
                                        .collect(Collectors.joining(", ", "[", "]"));


        String llmResponseJson;
        try {
            // llmResponseJson = professionalMatcher.findMatchingProfessionals(serviceRequest.getRequestDetails(), professionalsJson);
            // Placeholder response:
            String matchesJson = approvedProfessionals.stream()
                .limit(3) // Take at most 3 for placeholder
                .map(p -> String.format("{\"professionalId\":%d, \"rank\":%d, \"justification\":\"Placeholder match based on availability\", \"compatibilityScore\":%.1f}",
                                        p.getId(),
                                        approvedProfessionals.indexOf(p) + 1, // Simple rank
                                        1.0 - ( (double)approvedProfessionals.indexOf(p) / 10.0 ) // Simple score
                                       ))
                .collect(Collectors.joining(","));

            llmResponseJson = String.format(
                "{\"matches\":[%s], \"matchingCriteriaExplanation\":\"Placeholder: Top professionals selected based on simulated skill relevance and experience. LLM integration is not functional due to POM issues.\"}",
                matchesJson
            );

        } catch (Exception e) {
            serviceRequest.setMatchingCriteriaExplanation("Error during LLM matching process: " + e.getMessage() + ". Please try again later or contact support.");
            entityManager.merge(serviceRequest);
            throw new WebApplicationException("Error communicating with matching service.", e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        String explanation = "Could not parse LLM response.";
        if (llmResponseJson.contains("\"matchingCriteriaExplanation\":\"")) {
            try {
                // Basic parsing for placeholder
                int startIdx = llmResponseJson.indexOf("\"matchingCriteriaExplanation\":\"") + "\"matchingCriteriaExplanation\":\"".length();
                int endIdx = llmResponseJson.indexOf("\"", startIdx);
                if (endIdx > startIdx) {
                    explanation = llmResponseJson.substring(startIdx, endIdx);
                }
            } catch (Exception parseEx) {
                // log parsing error
                 System.err.println("Error parsing LLM explanation: " + parseEx.getMessage());
            }
        }

        serviceRequest.setMatchingCriteriaExplanation(explanation);
        serviceRequest.setStatus(ServiceRequestStatus.MATCHED);
        entityManager.merge(serviceRequest);
        return ServiceRequestViewDTO.fromEntity(serviceRequest);
    }
}
