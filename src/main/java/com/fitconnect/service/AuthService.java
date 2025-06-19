package com.fitconnect.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitconnect.dto.LoginRequest;
import com.fitconnect.dto.LoginResponse;
import com.fitconnect.dto.ProfessionalRegisterRequest;
import com.fitconnect.dto.RegisterRequest;
import com.fitconnect.entity.*;
import com.fitconnect.repository.ProfessionalRepository; // Added
import com.fitconnect.repository.UserRepository;
import io.smallrye.jwt.build.Jwt;
import org.jboss.resteasy.reactive.multipart.FileUpload; // Keep if planning to use for other things, or remove if only for documents here
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
// import jakarta.transaction.Transactional; // Removed
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
// Wildfly security imports removed

import java.io.IOException;
import java.nio.file.Files;
// import java.security.InvalidKeyException; // Removed (or ensure it's still needed)
// import java.security.NoSuchAlgorithmException; // Removed
// import java.security.spec.InvalidKeySpecException; // Removed
import java.time.Duration;
import java.util.ArrayList;
// import java.util.Arrays; // Removed
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException; // Added
import java.util.stream.Collectors; // Added

@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    @Inject
    UserRepository userRepository;

    @Inject
    ProfessionalRepository professionalRepository; // Added

    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String jwtIssuer;

    @Inject
    @ConfigProperty(name = "smallrye.jwt.sign.key.location")
    String privateKeyLocation;

    // PasswordFactory and constructor removed
    // hashPassword method removed
    // verifyPassword method removed

    /*
    public User registerClient(RegisterRequest request) throws ExecutionException, InterruptedException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        Client client = new Client();
        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhoneNumber(request.getPhoneNumber());
        userRepository.save(client);
        return client;
    }

    public Optional<LoginResponse> login(LoginRequest request) throws ExecutionException, InterruptedException {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        LOG.infof("searching user: " + request.getEmail());
        if (userOptional.isPresent()) {
            LOG.infof("user found");
            User user = userOptional.get();

            Set<String> roles = new HashSet<>();
            roles.add(user.getRole().name());

            // This JWT generation is for client-side, but with Firebase Auth, client gets token from Firebase.
            // Backend might issue its own token session or just rely on Firebase token verification per request.
            // For now, let's assume this was for the old system and can be removed for initializeUser flow.
            // String token = Jwt.issuer(jwtIssuer)
            //                   .upn(user.getEmail())
            //                   .subject(user.getId())
            //                   .groups(roles)
            //                   .expiresIn(Duration.ofHours(1))
            //                   .sign();

            String profileStatus = null;
            if (user.getRole() == UserRole.PROFESSIONAL) {
                Optional<Professional> professionalOptional = professionalRepository.findById(user.getId());
                if (professionalOptional.isPresent()) {
                    Professional professional = professionalOptional.get();
                    if (professional.getProfileStatus() != null) {
                        profileStatus = professional.getProfileStatus().name();
                    } else {
                        profileStatus = ProfileStatus.PENDING_VERIFICATION.name();
                    }
                } else {
                    LOG.warnf("Professional record not found in professionals collection for user ID: %s, who has PROFESSIONAL role.", user.getId());
                    profileStatus = ProfileStatus.PENDING_VERIFICATION.name();
                }
            } else {
                profileStatus = "N/A";
            }
            // Return LoginResponse without a new token, as client already has Firebase token
            return Optional.of(new LoginResponse(null, user.getId(), user.getEmail(), user.getRole().name(), profileStatus));
        }
        LOG.infof("user NOT found");
        return Optional.empty();
    }

    public Professional registerProfessional(ProfessionalRegisterRequest request) throws ExecutionException, InterruptedException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        Professional professional = new Professional();
        professional.setName(request.getName());
        professional.setEmail(request.getEmail());
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
                throw new RuntimeException("Invalid social media links format", e);
            }
        }

        professional.setRole(UserRole.PROFESSIONAL);
        professional.setProfileStatus(ProfileStatus.PENDING_VERIFICATION);

        User savedUserPortion = userRepository.save(professional);
        professional.setId(savedUserPortion.getId());
        professionalRepository.save(professional);

        return professional;
    }
    */

    public LoginResponse initializeUser(String uid, String email) throws ExecutionException, InterruptedException {
        Optional<User> userOptional = userRepository.findById(uid);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            LOG.infof("User %s (UID: %s) already exists. Returning existing user data.", email, uid);
            String profileStatus = "N/A";
            if (user.getRole() == UserRole.PROFESSIONAL) {
                Professional prof = professionalRepository.findById(uid)
                                        .orElseGet(() -> {
                                            // This case should ideally not happen if role is PROFESSIONAL
                                            // but user data is somehow missing in professionals collection.
                                            LOG.warnf("User UID %s has role PROFESSIONAL but no entry in professionals collection. Creating one.", uid);
                                            Professional newProf = new Professional();
                                            newProf.setId(uid);
                                            newProf.setEmail(email);
                                            newProf.setName(user.getName()); // Or a default name
                                            newProf.setRole(UserRole.PROFESSIONAL);
                                            newProf.setProfileStatus(ProfileStatus.PENDING_VERIFICATION);
                                            try {
                                                professionalRepository.save(newProf);
                                            } catch (ExecutionException | InterruptedException e) {
                                                LOG.error("Failed to save new professional stub", e);
                                                //Thread.currentThread().interrupt(); // Restore interrupt status
                                            }
                                            return newProf;
                                        });
                profileStatus = prof.getProfileStatus() != null ? prof.getProfileStatus().name() : ProfileStatus.PENDING_VERIFICATION.name();
            }
             // No new JWT token is issued here from backend; client uses Firebase ID token
            return new LoginResponse(null, uid, email, user.getRole().name(), profileStatus);
        } else {
            LOG.infof("User %s (UID: %s) not found. Creating new CLIENT user.", email, uid);
            User newUser = new User(); // Using User, not Client, as Client constructor sets role.
            newUser.setId(uid);
            newUser.setEmail(email);
            newUser.setRole(UserRole.CLIENT); // Default role for new users
            // Name can be set later via a profile update endpoint if not available from token
            // newUser.setName(displayNameFromTokenIfNotAvailableInEmail);
            userRepository.save(newUser);
             // No new JWT token is issued here
            return new LoginResponse(null, uid, email, UserRole.CLIENT.name(), "N/A");
        }
    }
}
