package com.fitconnect.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitconnect.dto.ProfessionalRegisterRequest;
import com.fitconnect.entity.Professional;
import com.fitconnect.entity.ProfessionalDocument;
import com.fitconnect.entity.User;
import com.fitconnect.entity.ProfileStatus;
import com.fitconnect.entity.UserRole;
import com.fitconnect.dto.ProfessionalProfileUpdateDTO;
import com.fitconnect.repository.ProfessionalDocumentRepository; // Added
import com.fitconnect.repository.ProfessionalRepository; // Added
import com.fitconnect.repository.SkillRepository; // Added
import com.fitconnect.repository.UserRepository; // Added

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
// import jakarta.transaction.Transactional; // Removed
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;
// Wildfly security imports removed

import java.io.IOException;
// import java.nio.file.Files; // Removed
// import java.nio.file.Path; // Removed
// import java.nio.file.Paths; // Removed
// import java.nio.file.StandardCopyOption; // Removed
// import java.security.spec.InvalidKeySpecException; // Removed
import java.util.*;
import java.util.concurrent.ExecutionException; // Added

@ApplicationScoped
public class ProfessionalService {

    private static final Logger LOG = Logger.getLogger(ProfessionalService.class);
    // private final Path UPLOAD_DIR = Paths.get("file-uploads", "professional-documents"); // Removed

    @Inject
    ObjectMapper objectMapper;

    @Inject
    UserRepository userRepository; // Added

    @Inject
    ProfessionalRepository professionalRepository; // Added

    @Inject
    SkillRepository skillRepository; // Added

    ProfessionalDocumentRepository professionalDocumentRepository;

    @Inject
    FirebaseStorageService storageService; // Added

    // PasswordFactory and constructor related to it removed
    public ProfessionalService() {
        // try {
            // Files.createDirectories(UPLOAD_DIR); // Removed
        // } catch (Exception e) {
            // LOG.error("Failed to create upload directory", e);
            // throw new RuntimeException("Service initialization failed", e);
        // }
    }

    // hashPassword method removed

    public Professional registerProfessional(ProfessionalRegisterRequest request) throws ExecutionException, InterruptedException { // Removed @Transactional, added throws
        if (userRepository.findByEmail(request.email).isPresent()) { // Changed to userRepository
            throw new IllegalArgumentException("Email already exists: " + request.email);
        }

        Professional pro = new Professional();
        pro.setName(request.name);
        pro.setEmail(request.email);
        // pro.setPassword(hashPassword(request.password)); // Password hashing removed
        pro.setPhoneNumber(request.phoneNumber);
        pro.setRole(UserRole.PROFESSIONAL);

        pro.profession = request.profession;
        pro.address = request.address;
        pro.postalCode = request.postalCode;
        pro.yearsOfExperience = request.yearsOfExperience;
        pro.qualifications = request.qualifications;
        pro.aboutYou = request.aboutYou;
        pro.profileStatus = ProfileStatus.PENDING_VERIFICATION;

        if (request.socialMediaLinksJson != null && !request.socialMediaLinksJson.isEmpty()) {
            try {
                Map<String, String> links = objectMapper.readValue(request.socialMediaLinksJson, new TypeReference<Map<String, String>>() {});
                pro.socialMediaLinks = links;
            } catch (IOException e) {
                LOG.warn("Could not parse socialMediaLinksJson: " + request.socialMediaLinksJson, e);
            }
        }

        User savedUserPart = userRepository.save(pro); // Save User part first
        pro.setId(savedUserPart.getId()); // Set ID for professional part

        professionalRepository.save(pro); // Save Professional specific data

        if (request.documents != null && !request.documents.isEmpty()) {
            if (pro.getProfessionalDocumentReferences() == null) {
                pro.setProfessionalDocumentReferences(new ArrayList<>());
            }
            for (FileUpload uploadedFile : request.documents) {
                try {
                    String originalFileName = uploadedFile.fileName();
                    String storagePath;
                    try {
                        storagePath = storageService.uploadFile(uploadedFile, "professional-documents");
                    } catch (IOException e) {
                        LOG.errorf(e, "Failed to upload document %s to Firebase Storage.", originalFileName);
                        // Depending on policy, might continue without this doc, or rethrow to fail registration
                        // For now, log and skip this document
                        continue;
                    }

                    ProfessionalDocument doc = new ProfessionalDocument();
                    doc.setProfessionalId(pro.getId());
                    doc.setFileName(originalFileName);
                    doc.setFileType(uploadedFile.contentType());
                    doc.setStoragePath(storagePath);

                    ProfessionalDocument savedDoc = professionalDocumentRepository.save(doc); // Save doc metadata
                    pro.getProfessionalDocumentReferences().add(savedDoc.getId()); // Add reference ID

                    LOG.infof("Stored document metadata for: %s for professional %s. Storage path: %s", originalFileName, pro.getEmail(), storagePath);

                } catch (Exception e) {
                    LOG.errorf(e, "Failed to process or save metadata for uploaded document: %s", uploadedFile.fileName());
                    // Log and continue, so one failed doc doesn't stop others or the whole registration.
                    // Consider adding it to a list of "failed uploads" to return to the user.
                }
            }
            if (pro.getProfessionalDocumentReferences() != null && !pro.getProfessionalDocumentReferences().isEmpty()) { // Check if not null before isEmpty
                pro.setUpdatedAt(java.time.LocalDateTime.now()); // Update timestamp due to document references change
                professionalRepository.save(pro); // Re-save professional with document references
            }
        }
        LOG.infof("Professional registered successfully: %s", pro.email);
        return pro;
    }

    public Professional getProfessionalById(String professionalId) throws ExecutionException, InterruptedException { // Changed Long to String, removed @Transactional, added throws
        Optional<Professional> professionalOptional = professionalRepository.findById(professionalId);
        if (professionalOptional.isEmpty()) {
            LOG.warnf("Professional with ID %s not found for getProfessionalById.", professionalId);
            throw new NotFoundException("Professional not found with ID: " + professionalId);
        }
        Professional professional = professionalOptional.get();

        // Load documents
        List<ProfessionalDocument> docs = professionalDocumentRepository.findByProfessionalId(professional.getId());
        // For now, docs are fetched but not directly set on professional unless a transient field is added.
        // DTOs would typically combine this data. LOG.debugf("Fetched %d documents for professional %s", docs.size(), professionalId);

        // Skills are already skillNames (List<String>) in Professional entity.
        // If full Skill objects are needed for a DTO:
        // List<Skill> fullSkills = new ArrayList<>();
        // if (professional.getSkillNames() != null) {
        //     for (String skillName : professional.getSkillNames()) {
        //         skillRepository.findByName(skillName).ifPresent(fullSkills::add);
        //     }
        // }
        // LOG.debugf("Fetched %d full skill objects for professional %s", fullSkills.size(), professionalId);

        return professional;
    }

    public Professional updateProfessionalProfile(String professionalId, ProfessionalProfileUpdateDTO dto) throws ExecutionException, InterruptedException { // Changed Long to String, removed @Transactional, added throws
        Optional<Professional> professionalOptional = professionalRepository.findById(professionalId);
        if (professionalOptional.isEmpty()) {
            LOG.warnf("Professional with ID %s not found for update.", professionalId);
            throw new NotFoundException("Professional not found with ID: " + professionalId);
        }
        Professional professional = professionalOptional.get();

        if (dto.getName() != null) professional.setName(dto.getName());
        if (dto.getPhoneNumber() != null) professional.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getProfession() != null) professional.profession = dto.getProfession();
        if (dto.getAddress() != null) professional.address = dto.getAddress();
        if (dto.getPostalCode() != null) professional.postalCode = dto.getPostalCode();
        if (dto.getYearsOfExperience() != null) professional.yearsOfExperience = dto.getYearsOfExperience();
        if (dto.getAboutYou() != null) professional.aboutYou = dto.getAboutYou();
        if (dto.getSocialMediaLinks() != null) professional.socialMediaLinks = dto.getSocialMediaLinks();
        if (dto.getQualifications() != null) professional.qualifications = dto.getQualifications();
        // Note: Updating skills (skillNames) and documents (professionalDocumentReferences)
        // would require more specific logic (add/remove individual items) - not covered by current DTO.
        professional.setUpdatedAt(java.time.LocalDateTime.now());
        professionalRepository.save(professional); // Changed from persist
        LOG.infof("Professional profile updated for ID %s.", professionalId);
        return professional;
    }
}
