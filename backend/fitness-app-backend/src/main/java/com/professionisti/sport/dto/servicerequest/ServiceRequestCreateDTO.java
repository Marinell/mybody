package com.professionisti.sport.dto.servicerequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ServiceRequestCreateDTO {

    @NotBlank(message = "Request details cannot be blank.")
    @Size(min = 10, max = 5000, message = "Request details must be between 10 and 5000 characters.")
    private String requestDetails;

    // Getters and Setters
    public String getRequestDetails() { return requestDetails; }
    public void setRequestDetails(String requestDetails) { this.requestDetails = requestDetails; }
}
