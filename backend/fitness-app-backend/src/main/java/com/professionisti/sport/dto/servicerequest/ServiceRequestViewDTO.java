package com.professionisti.sport.dto.servicerequest;

import com.professionisti.sport.model.enums.ServiceRequestStatus;
import java.time.LocalDateTime;

public class ServiceRequestViewDTO {
    private Long id;
    private String requestDetails;
    private ServiceRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long clientId;
    private String clientEmail;
    private Long chosenProfessionalId;
    private String chosenProfessionalName;
    private String matchingCriteriaExplanation;

    // Constructor
    public ServiceRequestViewDTO(Long id, String requestDetails, ServiceRequestStatus status,
                                 LocalDateTime createdAt, LocalDateTime updatedAt, Long clientId, String clientEmail,
                                 Long chosenProfessionalId, String chosenProfessionalName, String matchingCriteriaExplanation) {
        this.id = id;
        this.requestDetails = requestDetails;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.clientId = clientId;
        this.clientEmail = clientEmail;
        this.chosenProfessionalId = chosenProfessionalId;
        this.chosenProfessionalName = chosenProfessionalName;
        this.matchingCriteriaExplanation = matchingCriteriaExplanation;
    }

    public static ServiceRequestViewDTO fromEntity(com.professionisti.sport.model.ServiceRequest entity) {
        if (entity == null) return null;
        String profName = null;
        if (entity.getChosenProfessional() != null) {
            String firstName = entity.getChosenProfessional().getFirstName() != null ? entity.getChosenProfessional().getFirstName() : "";
            String lastName = entity.getChosenProfessional().getLastName() != null ? entity.getChosenProfessional().getLastName() : "";
            profName = (firstName + " " + lastName).trim();
            if (profName.isEmpty()) profName = null;
        }

        return new ServiceRequestViewDTO(
            entity.getId(),
            entity.getRequestDetails(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getClient() != null ? entity.getClient().getId() : null,
            entity.getClient() != null ? entity.getClient().getEmail() : null,
            entity.getChosenProfessional() != null ? entity.getChosenProfessional().getId() : null,
            profName,
            entity.getMatchingCriteriaExplanation()
        );
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestDetails() { return requestDetails; }
    public void setRequestDetails(String requestDetails) { this.requestDetails = requestDetails; }
    public ServiceRequestStatus getStatus() { return status; }
    public void setStatus(ServiceRequestStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    public Long getChosenProfessionalId() { return chosenProfessionalId; }
    public void setChosenProfessionalId(Long chosenProfessionalId) { this.chosenProfessionalId = chosenProfessionalId; }
    public String getChosenProfessionalName() { return chosenProfessionalName; }
    public void setChosenProfessionalName(String chosenProfessionalName) { this.chosenProfessionalName = chosenProfessionalName; }
    public String getMatchingCriteriaExplanation() { return matchingCriteriaExplanation; }
    public void setMatchingCriteriaExplanation(String matchingCriteriaExplanation) { this.matchingCriteriaExplanation = matchingCriteriaExplanation; }
}
