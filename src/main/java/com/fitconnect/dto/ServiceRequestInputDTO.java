package com.fitconnect.dto;

// DTO for creating a new service request
public class ServiceRequestInputDTO {
    private String category;
    private String serviceDescription;
    private String budget; // e.g., "50-100", "negotiable"

    // Getters and Setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }
}
