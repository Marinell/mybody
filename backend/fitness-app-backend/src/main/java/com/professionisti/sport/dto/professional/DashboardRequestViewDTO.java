package com.professionisti.sport.dto.professional;

import com.professionisti.sport.model.ServiceRequest;
import com.professionisti.sport.model.enums.ServiceRequestStatus;
import java.time.LocalDateTime;

public class DashboardRequestViewDTO {
    private Long serviceRequestId;
    private String requestDetails;
    private ServiceRequestStatus status;
    private LocalDateTime requestCreatedAt;
    private String clientFirstName;
    private String clientLastName;
    private String clientEmail; // Contact info for professional
    // Phone number could be added if Client entity has it and it's permitted

    public DashboardRequestViewDTO(Long serviceRequestId, String requestDetails, ServiceRequestStatus status,
                                   LocalDateTime requestCreatedAt, String clientFirstName, String clientLastName, String clientEmail) {
        this.serviceRequestId = serviceRequestId;
        this.requestDetails = requestDetails;
        this.status = status;
        this.requestCreatedAt = requestCreatedAt;
        this.clientFirstName = clientFirstName;
        this.clientLastName = clientLastName;
        this.clientEmail = clientEmail;
    }

    public static DashboardRequestViewDTO fromEntity(ServiceRequest entity) {
        if (entity == null || entity.getClient() == null) {
            return null;
        }
        return new DashboardRequestViewDTO(
            entity.getId(),
            entity.getRequestDetails(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getClient().getFirstName(),
            entity.getClient().getLastName(),
            entity.getClient().getEmail() // Providing client's email to professional
        );
    }

    // Getters
    public Long getServiceRequestId() { return serviceRequestId; }
    public String getRequestDetails() { return requestDetails; }
    public ServiceRequestStatus getStatus() { return status; }
    public LocalDateTime getRequestCreatedAt() { return requestCreatedAt; }
    public String getClientFirstName() { return clientFirstName; }
    public String getClientLastName() { return clientLastName; }
    public String getClientEmail() { return clientEmail; }
}
