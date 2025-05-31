package com.professionisti.sport.service;

import com.professionisti.sport.dto.auth.ClientRegistrationRequest;
import com.professionisti.sport.dto.auth.LoginRequest;
import com.professionisti.sport.dto.auth.LoginResponse;
import com.professionisti.sport.dto.auth.ProfessionalRegistrationDetails;
import com.professionisti.sport.dto.professional.ProfessionalPublicViewDTO;
import com.professionisti.sport.dto.professional.DashboardRequestViewDTO; // Added
import com.professionisti.sport.model.Client;
import com.professionisti.sport.model.Document;
import com.professionisti.sport.model.Professional;
import com.professionisti.sport.model.User;
import com.professionisti.sport.model.ServiceRequest; // Added
import com.professionisti.sport.model.enums.Role;
import com.professionisti.sport.model.enums.ScreeningStatus;
import com.professionisti.sport.model.enums.ServiceRequestStatus; // Added
import com.professionisti.sport.util.PasswordUtil;
import com.professionisti.sport.util.JwtUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery; // Added
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections; // Added
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Added

@ApplicationScoped
public class UserService {

    @Inject
    EntityManager entityManager;

    @Transactional
    public Client registerClient(ClientRegistrationRequest request) {
        if (emailExists(request.getEmail())) {
            throw new WebApplicationException("Email already exists", Response.Status.CONFLICT);
        }
        Client client = new Client();
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setEmail(request.getEmail());
        client.setPassword(PasswordUtil.hashPassword(request.getPassword()));
        client.setRole(Role.ROLE_CLIENT);
        entityManager.persist(client);
        return client;
    }

    @Transactional
    public Professional registerProfessional(ProfessionalRegistrationDetails details, List<FileUpload> documentUploads) {
        if (emailExists(details.getEmail())) {
            throw new WebApplicationException("Email already exists", Response.Status.CONFLICT);
        }
        Professional professional = new Professional();
        professional.setFirstName(details.getFirstName());
        professional.setLastName(details.getLastName());
        professional.setEmail(details.getEmail());
        professional.setPassword(PasswordUtil.hashPassword(details.getPassword()));
        professional.setRole(Role.ROLE_PROFESSIONAL);
        professional.setPhoneNumber(details.getPhoneNumber());
        professional.setSpecialization(details.getSpecialization());
        professional.setExperienceYears(details.getExperienceYears());
        professional.setBlogUrl(details.getBlogUrl());
        professional.setSocialMediaLink(details.getSocialMediaLink());
        professional.setScreeningStatus(ScreeningStatus.PENDING);
        if (professional.getDocuments() == null) { // Initialize documents list
            professional.setDocuments(new ArrayList<>());
        }
        entityManager.persist(professional);

        if (documentUploads != null) {
            for (FileUpload fileUpload : documentUploads) {
                if (fileUpload == null || fileUpload.fileName() == null || fileUpload.fileName().isEmpty() ) continue;
                Path uploadedFilePath = fileUpload.uploadedFile();
                 if (uploadedFilePath == null || !Files.exists(uploadedFilePath)) {
                    System.err.println("Uploaded file path is invalid or file does not exist for: " + fileUpload.fileName());
                    continue;
                }
                try {
                    Document doc = new Document();
                    doc.setFileName(fileUpload.fileName());
                    doc.setFileType(fileUpload.contentType());
                    doc.setData(Files.readAllBytes(uploadedFilePath));
                    doc.setProfessional(professional);
                    entityManager.persist(doc);
                    professional.getDocuments().add(doc);
                } catch (IOException e) {
                    throw new WebApplicationException("Error processing uploaded file: " + fileUpload.fileName(), e, Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return professional;
    }

    public Optional<User> findUserByEmail(String email) {
        try {
            User user = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public boolean emailExists(String email) {
        return findUserByEmail(email).isPresent();
    }

    public LoginResponse login(LoginRequest request) {
        User user = findUserByEmail(request.getEmail())
                .orElseThrow(() -> new WebApplicationException("Invalid email or password", Response.Status.UNAUTHORIZED));
        if (!PasswordUtil.verifyPassword(request.getPassword(), user.getPassword())) {
            throw new WebApplicationException("Invalid email or password", Response.Status.UNAUTHORIZED);
        }
        String token = JwtUtil.generateToken(user);
        return new LoginResponse(token, "Login successful", user.getEmail(), user.getRole().name());
    }

    public ProfessionalPublicViewDTO getApprovedProfessionalDetails(Long professionalId) {
        Professional professional = entityManager.find(Professional.class, professionalId);
        if (professional == null || professional.getScreeningStatus() != ScreeningStatus.APPROVED) {
            throw new WebApplicationException("Approved professional not found with ID: " + professionalId, Response.Status.NOT_FOUND);
        }
        return ProfessionalPublicViewDTO.fromEntity(professional);
    }

    public List<DashboardRequestViewDTO> getDashboardRequests(String professionalEmail) {
        Optional<User> userOpt = findUserByEmail(professionalEmail);
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Professional)) {
            throw new WebApplicationException("Professional not found or user is not a professional.", Response.Status.FORBIDDEN);
        }
        Professional professional = (Professional) userOpt.get();

        TypedQuery<ServiceRequest> query = entityManager.createQuery(
            "SELECT sr FROM ServiceRequest sr WHERE sr.chosenProfessional = :professional AND sr.status = :status ORDER BY sr.updatedAt DESC",
            ServiceRequest.class
        );
        query.setParameter("professional", professional);
        query.setParameter("status", ServiceRequestStatus.IN_PROGRESS);

        List<ServiceRequest> requests = query.getResultList();
        if (requests == null) { // Should not happen with getResultList, but good practice
            return Collections.emptyList();
        }
        return requests.stream()
                       .map(DashboardRequestViewDTO::fromEntity)
                       .collect(Collectors.toList());
    }
}
