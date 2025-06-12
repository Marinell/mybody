package com.fitconnect.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

// Using a single DTO for registration for now, can be specialized
// for Professional and Client if fields diverge significantly beyond User fields.
@Data
@NoArgsConstructor
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    // Add professional-specific fields if this DTO is used for professional registration directly
    // For now, assuming separate more detailed DTOs or service logic will handle this.
}
