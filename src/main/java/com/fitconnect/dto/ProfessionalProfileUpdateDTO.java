package com.fitconnect.dto;

import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

// Contains fields a professional can update for their own profile.
@Data
@NoArgsConstructor
public class ProfessionalProfileUpdateDTO {
    // Not all fields are updatable directly, e.g., email (usually part of auth identity), status.
    // Name and phone number might be updatable via a general User update DTO if separated.
    // For now, including some common ones here.
    private String name; // Assuming name can be updated
    private String phoneNumber;
    private String profession;
    private String address;
    private String postalCode;
    private Integer yearsOfExperience;
    private String qualifications; // If updated, might require re-verification
    private String aboutYou;
    private Map<String, String> socialMediaLinks;
    // Password update should be a separate endpoint/flow.
    // Document updates (add/delete) would also be separate if complex.
}
