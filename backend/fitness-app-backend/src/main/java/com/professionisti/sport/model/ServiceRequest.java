package com.professionisti.sport.model;

import com.professionisti.sport.model.enums.ServiceRequestStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String requestDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequestStatus status;

    // To store the professional chosen by the client after matching
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chosen_professional_id", nullable = true)
    private Professional chosenProfessional;

    @Column(columnDefinition = "TEXT")
    private String matchingCriteriaExplanation; // To store why certain professionals were matched

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public String getRequestDetails() { return requestDetails; }
    public void setRequestDetails(String requestDetails) { this.requestDetails = requestDetails; }
    public ServiceRequestStatus getStatus() { return status; }
    public void setStatus(ServiceRequestStatus status) { this.status = status; }
    public Professional getChosenProfessional() { return chosenProfessional; }
    public void setChosenProfessional(Professional chosenProfessional) { this.chosenProfessional = chosenProfessional; }
    public String getMatchingCriteriaExplanation() { return matchingCriteriaExplanation; }
    public void setMatchingCriteriaExplanation(String matchingCriteriaExplanation) { this.matchingCriteriaExplanation = matchingCriteriaExplanation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ServiceRequestStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
