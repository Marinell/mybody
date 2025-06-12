package com.fitconnect.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

// DTO for creating a new service request
@Data
@NoArgsConstructor
public class ServiceRequestInputDTO {
    private String category;
    private String serviceDescription;
    private String budget; // e.g., "50-100", "negotiable"
}
