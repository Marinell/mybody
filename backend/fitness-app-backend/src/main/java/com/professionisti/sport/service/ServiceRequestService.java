package com.professionisti.sport.service;

import com.professionisti.sport.dto.servicerequest.ServiceRequestCreateDTO;
import com.professionisti.sport.dto.servicerequest.ServiceRequestViewDTO;
import com.professionisti.sport.model.Client;
import com.professionisti.sport.model.Professional; // Added
import com.professionisti.sport.model.ServiceRequest;
import com.professionisti.sport.model.User;
import com.professionisti.sport.model.enums.ScreeningStatus; // Added
import com.professionisti.sport.model.enums.ServiceRequestStatus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@ApplicationScoped
public class ServiceRequestService {

    @Inject
    EntityManager entityManager;

    @Inject
    UserService userService; // To find client by email

    @Transactional
    public ServiceRequestViewDTO createServiceRequest(ServiceRequestCreateDTO createDTO, String clientEmail) {
        Optional<User> userOpt = userService.findUserByEmail(clientEmail);
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Client)) {
            throw new WebApplicationException("Client not found or user is not a client: " + clientEmail, Response.Status.FORBIDDEN);
        }
        Client client = (Client) userOpt.get();

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setClient(client);
        serviceRequest.setRequestDetails(createDTO.getRequestDetails());
        // serviceRequest.setStatus(ServiceRequestStatus.OPEN); // Initial status set by @PrePersist in entity
        entityManager.persist(serviceRequest);
        return ServiceRequestViewDTO.fromEntity(serviceRequest);
    }

    public ServiceRequestViewDTO findServiceRequestById(Long id, String clientEmail) {
        Optional<User> userOpt = userService.findUserByEmail(clientEmail);
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Client)) {
            throw new WebApplicationException("Client not found or user is not a client: " + clientEmail, Response.Status.FORBIDDEN);
        }
        Client client = (Client) userOpt.get();

        ServiceRequest serviceRequest = entityManager.find(ServiceRequest.class, id);
        if (serviceRequest == null) {
            throw new WebApplicationException("Service request not found with ID: " + id, Response.Status.NOT_FOUND);
        }
        if (serviceRequest.getClient() == null || !serviceRequest.getClient().getId().equals(client.getId())) {
            throw new WebApplicationException("Client not authorized to view this service request.", Response.Status.FORBIDDEN);
        }
        return ServiceRequestViewDTO.fromEntity(serviceRequest);
    }

    @Transactional
    public ServiceRequestViewDTO chooseProfessional(Long serviceRequestId, Long professionalId, String clientEmail) {
        // Verify client
        Optional<User> userOpt = userService.findUserByEmail(clientEmail);
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Client)) {
            throw new WebApplicationException("Client not found or user is not a client: " + clientEmail, Response.Status.FORBIDDEN);
        }
        Client client = (Client) userOpt.get();

        // Get Service Request
        ServiceRequest serviceRequest = entityManager.find(ServiceRequest.class, serviceRequestId);
        if (serviceRequest == null) {
            throw new WebApplicationException("Service request not found: " + serviceRequestId, Response.Status.NOT_FOUND);
        }

        // Verify ownership and state
        if (serviceRequest.getClient() == null || !serviceRequest.getClient().getId().equals(client.getId())) {
            throw new WebApplicationException("Client not authorized for this service request.", Response.Status.FORBIDDEN);
        }
        if (serviceRequest.getStatus() != ServiceRequestStatus.MATCHED) {
            throw new WebApplicationException("Service request is not in MATCHED state. Current state: " + serviceRequest.getStatus(), Response.Status.CONFLICT);
        }
        if (serviceRequest.getChosenProfessional() != null) {
            throw new WebApplicationException("A professional has already been chosen for this request.", Response.Status.CONFLICT);
        }

        // Get Professional
        Professional professional = entityManager.find(Professional.class, professionalId);
        if (professional == null || professional.getScreeningStatus() != ScreeningStatus.APPROVED) {
            throw new WebApplicationException("Chosen professional not found or not approved.", Response.Status.BAD_REQUEST);
        }

        serviceRequest.setChosenProfessional(professional);
        serviceRequest.setStatus(ServiceRequestStatus.IN_PROGRESS);
        entityManager.merge(serviceRequest);
        return ServiceRequestViewDTO.fromEntity(serviceRequest);
    }
}
