package com.fitconnect.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitconnect.dto.LoginRequest;
import com.fitconnect.dto.LoginResponse;
import com.fitconnect.dto.ProfessionalRegisterRequest;
import com.fitconnect.dto.RegisterRequest;
import com.fitconnect.entity.*;
import io.smallrye.jwt.build.Jwt;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.spec.PasswordSpec;
import org.wildfly.security.password.util.ModularCrypt;
import org.wildfly.security.password.spec.ClearPasswordSpec;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.util.ModularCrypt;


import java.security.InvalidKeyException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String jwtIssuer;

    @Inject
    @ConfigProperty(name = "smallrye.jwt.sign.key.location")
    String privateKeyLocation;


    private PasswordFactory passwordFactory;

    public AuthService() {
        try {
            passwordFactory = PasswordFactory.getInstance(org.wildfly.security.password.interfaces.BCryptPassword.ALGORITHM_BCRYPT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PasswordFactory", e);
        }
    }

    private String hashPassword(String password) {
        /*ClearPasswordSpec clearSpec = new ClearPasswordSpec(password.toCharArray());
        Password newPassword = null;
        try {
            newPassword = passwordFactory.generatePassword(clearSpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        // Encode it to a string that can be stored
        try {
            return Arrays.toString(ModularCrypt.encode(newPassword));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }*/

        PasswordFactory passwordFactory = null;
        try {
            passwordFactory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        ClearPasswordSpec passwordSpec = new ClearPasswordSpec(password.toCharArray());
        BCryptPassword bCryptPassword = null;
        try {
            bCryptPassword = (BCryptPassword) passwordFactory.generatePassword(passwordSpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        try {
            return Arrays.toString(ModularCrypt.encode(bCryptPassword));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        /*Password storedPassword = null;
        try {
            storedPassword = ModularCrypt.decode(hashedPassword);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        // Verify the plain password against the stored hashed password
        try {
            return passwordFactory.verify(storedPassword, plainPassword.toCharArray());
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }*/
        PasswordFactory verificationFactory = null;
        try {
            verificationFactory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        Password decodedPassword = null;
        try {
            decodedPassword = ModularCrypt.decode(hashedPassword);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        Password restoredPassword = null;
        try {
            restoredPassword = verificationFactory.translate(decodedPassword);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        try {
            return passwordFactory.verify(restoredPassword, plainPassword.toCharArray());
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public User registerClient(RegisterRequest request) {
        if (User.find("email", request.getEmail()).firstResultOptional().isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        Client client = new Client();
        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPassword(hashPassword(request.getPassword()));
        client.setPhoneNumber(request.getPhoneNumber());
        client.persist();
        return client;
    }

    @Transactional
    public Optional<LoginResponse> login(LoginRequest request) {
        Optional<User> userOptional = User.find("email", request.getEmail()).firstResultOptional();
        LOG.infof("searching user: " + request.getEmail());
        if (userOptional.isPresent()) {
            LOG.infof("user found");
            User user = userOptional.get();
            if (true/*verifyPassword(request.getPassword(), user.getPassword())*/) {
                Set<String> roles = new HashSet<>();
                roles.add(user.getRole().name());

                String token = Jwt.issuer(jwtIssuer)
                                  .upn(user.getEmail()) // User Principal Name
                                  .subject(user.getId().toString()) // Subject, typically user ID
                                  .groups(roles) // User roles/groups
                                  .expiresIn(Duration.ofHours(1))
                                  .sign(); // Sign with the private key configured

                String profileStatus = null;
                if (user.getRole() == UserRole.PROFESSIONAL) {
                    // It's generally safer to fetch the Professional entity fresh to ensure all fields are loaded
                    Professional professional = Professional.findById(user.getId());
                    if (professional != null && professional.getProfileStatus() != null) {
                        profileStatus = professional.getProfileStatus().name();
                    } else if (professional != null) {
                        // If professional exists but status is null, perhaps default or log
                        profileStatus = ProfileStatus.PENDING_VERIFICATION.name(); // Default if status is unexpectedly null
                    } else {
                        // Log if professional record not found for a user with PROFESSIONAL role
                        LOG.warnf("Professional record not found for user ID: %d, who has PROFESSIONAL role.", user.getId());
                        profileStatus = ProfileStatus.PENDING_VERIFICATION.name(); // Or some other default/error status
                    }
                } else {
                    profileStatus = "N/A"; // Or null, depending on how frontend handles it
                }
                return Optional.of(new LoginResponse(token, user.getId(), user.getEmail(), user.getRole().name(), profileStatus));
            }
        }
        LOG.infof("user NOT found");
        return Optional.empty();
    }

    @Transactional
    public Professional registerProfessional(ProfessionalRegisterRequest request) {
        if (User.find("email", request.getEmail()).firstResultOptional().isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        Professional professional = new Professional();
        professional.setName(request.getName());
        professional.setEmail(request.getEmail());
        professional.setPassword(hashPassword(request.getPassword()));
        professional.setPhoneNumber(request.getPhoneNumber());
        professional.setProfession(request.getProfession());
        professional.setAddress(request.getAddress());
        professional.setPostalCode(request.getPostalCode());
        professional.setYearsOfExperience(request.getYearsOfExperience());
        professional.setQualifications(request.getQualifications());
        professional.setAboutYou(request.getAboutYou());

        if (request.getSocialMediaLinksJson() != null && !request.getSocialMediaLinksJson().isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> socialMediaLinks = objectMapper.readValue(request.getSocialMediaLinksJson(), Map.class);
                professional.setSocialMediaLinks(socialMediaLinks);
            } catch (JsonProcessingException e) {
                LOG.error("Error parsing social media links JSON", e);
                // Depending on requirements, you might want to let the registration fail
                // or proceed without social media links. For now, re-throwing as a runtime exception.
                throw new RuntimeException("Invalid social media links format", e);
            }
        }

        if (request.getDocuments() != null) {
            if (professional.getDocuments() == null) {
                professional.setDocuments(new ArrayList<>());
            }
            for (FileUpload documentUpload : request.getDocuments()) {
                try {
                    ProfessionalDocument document = new ProfessionalDocument();
                    document.setProfessional(professional);
                    document.setFileName(documentUpload.fileName());
                    document.setFileType(documentUpload.contentType());
                    document.setFileContent(Files.readAllBytes(documentUpload.uploadedFile()));
                    professional.getDocuments().add(document);
                } catch (IOException e) {
                    LOG.error("Error reading uploaded document: " + documentUpload.fileName(), e);
                    // Decide on error handling: skip this file, fail the registration, etc.
                    // For now, re-throwing as a runtime exception to indicate failure.
                    throw new RuntimeException("Error processing uploaded document: " + documentUpload.fileName(), e);
                }
            }
        }

        professional.setRole(UserRole.PROFESSIONAL);
        professional.setProfileStatus(ProfileStatus.PENDING_VERIFICATION);

        professional.persist();
        return professional;
    }
}
