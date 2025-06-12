package com.fitconnect.dto;

import com.fitconnect.entity.UserRole;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ProfessionalRegisterRequest {

    @RestForm("name")
    public String name;

    @RestForm("email")
    public String email;

    @RestForm("password")
    public String password;

    @RestForm("phoneNumber")
    public String phoneNumber;

    @RestForm("profession")
    public String profession;

    @RestForm("address")
    public String address;

    @RestForm("postalCode")
    public String postalCode;

    @RestForm("yearsOfExperience")
    public Integer yearsOfExperience;

    @RestForm("qualifications")
    public String qualifications; // Text area

    @RestForm("aboutYou")
    public String aboutYou; // Text area

    // For social media links, expecting them as form fields like socialMediaLinks[linkedin], socialMediaLinks[website]
    // RESTEasy Reactive might require a custom way to map these or receive them as a JSON string part.
    // For simplicity in this subtask, we'll assume they come as individual fields or a JSON string that we parse.
    // Let's assume individual fields for now, and the client will send them prefixed.
    // Or, more practically, a single JSON string for all links.
    @RestForm("socialMediaLinksJson") // e.g., {"linkedin": "url", "website": "url"}
    public String socialMediaLinksJson;

    // File uploads
    // The HTML form should use name="documents" for file input fields
    @RestForm("documents")
    @PartType(MediaType.APPLICATION_OCTET_STREAM) // Or specific types if known
    public List<FileUpload> documents; // List to handle multiple file uploads

    // Getters (and setters if needed by framework, but public fields are often fine for DTOs in Quarkus)
    // No explicit getters/setters needed for public fields when using RESTEasy Reactive binding.
}
