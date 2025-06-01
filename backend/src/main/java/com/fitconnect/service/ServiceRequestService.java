package com.fitconnect.service;

import com.fitconnect.dto.ServiceRequestInputDTO;
import com.fitconnect.entity.Client;
import com.fitconnect.entity.ServiceRequest;
import com.fitconnect.entity.ServiceRequestStatus;
import com.fitconnect.entity.Professional; // Added
import com.fitconnect.entity.Appointment; // Added
import com.fitconnect.entity.AppointmentStatus; // Added

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class ServiceRequestService {

    private static final Logger LOG = Logger.getLogger(ServiceRequestService.class);

    @Transactional
    public ServiceRequest createServiceRequest(ServiceRequestInputDTO dto, Long clientId) {
        Client client = Client.findById(clientId);
        if (client == null) {
            LOG.warnf("Client with ID %d not found when trying to create service request.", clientId);
            throw new NotFoundException("Client not found with ID: " + clientId);
        }

        ServiceRequest request = new ServiceRequest();
        request.setClient(client);
        request.setCategory(dto.getCategory());
        request.setServiceDescription(dto.getServiceDescription());
        request.setBudget(dto.getBudget());

        request.persist();
        LOG.infof("Service request created with ID %d for client ID %d.", request.id, clientId);
        return request;
    }

    @Transactional
    public Appointment selectProfessionalForServiceRequest(Long serviceRequestId, Long professionalId, Long clientId) {
        ServiceRequest serviceRequest = ServiceRequest.findById(serviceRequestId);
        if (serviceRequest == null) {
            throw new NotFoundException("ServiceRequest not found with ID: " + serviceRequestId);
        }
        if (!serviceRequest.getClient().id.equals(clientId)) {
            LOG.warnf("Client ID %d attempted to select professional for service request %d not owned by them.", clientId, serviceRequestId);
            throw new jakarta.ws.rs.ForbiddenException("You are not authorized to modify this service request.");
        }
        if (serviceRequest.getStatus() != ServiceRequestStatus.OPEN && serviceRequest.getStatus() != ServiceRequestStatus.MATCHED) {
            // Assuming MATCHED is a status after LLM provides options but before client selects one.
            // If not, adjust this logic. For now, let's assume OPEN is fine.
            LOG.warnf("Service request %d is not in a state where a professional can be selected. Current status: %s", serviceRequestId, serviceRequest.getStatus());
            throw new IllegalStateException("Professional can only be selected for OPEN or MATCHED service requests.");
        }

        Professional professional = Professional.findById(professionalId);
        if (professional == null) {
            throw new NotFoundException("Professional not found with ID: " + professionalId);
        }

        // Check if an appointment already exists for this service request
        Appointment existingAppointment = Appointment.find("serviceRequest", serviceRequest).firstResult();
        if (existingAppointment != null) {
            LOG.warnf("Client ID %d attempted to select professional for service request %d which already has an appointment %d.", clientId, serviceRequestId, existingAppointment.id);
            throw new IllegalStateException("A professional has already been selected or an appointment exists for this service request.");
        }

        Appointment appointment = new Appointment();
        appointment.setClient(serviceRequest.getClient());
        appointment.setProfessional(professional);
        appointment.setServiceRequest(serviceRequest);
        appointment.setStatus(AppointmentStatus.REQUESTED); // Professional needs to see this request
        // appointment.setAppointmentDateTime(null); // To be set later if needed
        appointment.setCommunicationDetails(String.format("Client %s selected professional %s. Contact via details on profile.",
                                                            serviceRequest.getClient().getName(), professional.getName()));
        appointment.persist();

        serviceRequest.setStatus(ServiceRequestStatus.PENDING_CONTACT); // Or a similar status like AWAITING_PROFESSIONAL
        serviceRequest.persist();

        LOG.infof("Client ID %d selected Professional ID %d for ServiceRequest ID %d. Appointment ID %d created.", clientId, professionalId, serviceRequestId, appointment.id);
        return appointment;
    }
}
