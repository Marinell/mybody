package com.fitconnect.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitconnect.dto.ProfessionalRegisterRequest;
import com.fitconnect.entity.Professional;
import com.fitconnect.entity.ProfessionalDocument;
import com.fitconnect.entity.User;
import com.fitconnect.entity.ProfileStatus;
import com.fitconnect.entity.UserRole;
import com.fitconnect.dto.ProfessionalProfileUpdateDTO; // Added

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
// import jakarta.ws.rs.ForbiddenException; // Not used in this service directly
import jakarta.ws.rs.NotFoundException; // Added
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.spec.ClearPasswordSpec;
import org.wildfly.security.password.util.ModularCrypt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class ProfessionalService {

    private static final Logger LOG = Logger.getLogger(ProfessionalService.class);
    private final Path UPLOAD_DIR = Paths.get("file-uploads", "professional-documents");

    @Inject
    ObjectMapper objectMapper;

    private PasswordFactory passwordFactory;

    public ProfessionalService() {
        try {
            passwordFactory = PasswordFactory.getInstance(org.wildfly.security.password.interfaces.BCryptPassword.ALGORITHM_BCRYPT);
            Files.createDirectories(UPLOAD_DIR);
        } catch (Exception e) {
            LOG.error("Failed to initialize PasswordFactory or create upload directory", e);
            throw new RuntimeException("Service initialization failed", e);
        }
    }

    private String hashPassword(String password) {
        ClearPasswordSpec clearSpec = new ClearPasswordSpec(password.toCharArray());
        Password newPassword = passwordFactory.generatePassword(clearSpec);
        return ModularCrypt.encode(newPassword);
    }

    @Transactional
    public Professional registerProfessional(ProfessionalRegisterRequest request) {
        if (User.find("email", request.email).firstResultOptional().isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.email);
        }

        Professional pro = new Professional();
        pro.setName(request.name);
        pro.setEmail(request.email);
        pro.setPassword(hashPassword(request.password));
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

        pro.persist();

        List<ProfessionalDocument> profDocs = new ArrayList<>();
        if (request.documents != null && !request.documents.isEmpty()) {
            for (FileUpload uploadedFile : request.documents) {
                try {
                    String originalFileName = uploadedFile.fileName();
                    String extension = "";
                    int i = originalFileName.lastIndexOf('.');
                    if (i > 0) {
                        extension = originalFileName.substring(i);
                    }
                    String newFileName = UUID.randomUUID().toString() + extension;
                    Path filePath = UPLOAD_DIR.resolve(newFileName);

                    Files.copy(uploadedFile.uploadedFile(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    ProfessionalDocument doc = new ProfessionalDocument();
                    doc.setProfessional(pro);
                    doc.setFileName(originalFileName);
                    doc.setFileType(uploadedFile.contentType());
                    doc.setStoragePath(filePath.toString());
                    doc.persist();
                    profDocs.add(doc);
                    LOG.infof("Stored document: %s as %s for professional %s", originalFileName, newFileName, pro.email);

                } catch (IOException e) {
                    LOG.error("Failed to store uploaded document: " + uploadedFile.fileName(), e);
                    throw new RuntimeException("Failed to store document: " + uploadedFile.fileName(), e);
                }
            }
        }
        pro.documents = profDocs;

        LOG.infof("Professional registered successfully: %s", pro.email);
        return pro;
    }

    @Transactional
    public Professional getProfessionalById(Long professionalId) {
        Professional professional = Professional.findById(professionalId);
        if (professional == null) {
            LOG.warnf("Professional with ID %d not found for getProfessionalById.", professionalId);
            throw new NotFoundException("Professional not found with ID: " + professionalId);
        }
        // Trigger loading for DTO if necessary, e.g. by accessing size
        if (professional.documents != null) professional.documents.size();
        if (professional.skills != null) professional.skills.size();
        return professional;
    }

    @Transactional
    public Professional updateProfessionalProfile(Long professionalId, ProfessionalProfileUpdateDTO dto) {
        Professional professional = Professional.findById(professionalId);
        if (professional == null) {
            LOG.warnf("Professional with ID %d not found for update.", professionalId);
            throw new NotFoundException("Professional not found with ID: " + professionalId);
        }

        if (dto.getName() != null) professional.setName(dto.getName());
        if (dto.getPhoneNumber() != null) professional.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getProfession() != null) professional.profession = dto.getProfession();
        if (dto.getAddress() != null) professional.address = dto.getAddress();
        if (dto.getPostalCode() != null) professional.postalCode = dto.getPostalCode();
        if (dto.getYearsOfExperience() != null) professional.yearsOfExperience = dto.getYearsOfExperience();
        if (dto.getAboutYou() != null) professional.aboutYou = dto.getAboutYou();
        if (dto.getSocialMediaLinks() != null) professional.socialMediaLinks = dto.getSocialMediaLinks();

        if (dto.getQualifications() != null) {
            professional.qualifications = dto.getQualifications();
        }

        professional.persist();
        LOG.infof("Professional profile updated for ID %d.", professionalId);
        return professional;
    }
}
