package com.fitconnect.dto;

// Using a single DTO for registration for now, can be specialized
// for Professional and Client if fields diverge significantly beyond User fields.
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    // Add professional-specific fields if this DTO is used for professional registration directly
    // For now, assuming separate more detailed DTOs or service logic will handle this.

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
