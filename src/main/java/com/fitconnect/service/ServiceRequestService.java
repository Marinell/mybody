package com.fitconnect.service;

import com.fitconnect.dto.ServiceRequestInputDTO;
import com.fitconnect.entity.ServiceRequest;
import com.fitconnect.entity.ServiceRequestStatus;
import com.fitconnect.entity.Professional;
import com.fitconnect.entity.Appointment;
import com.fitconnect.entity.AppointmentStatus;
import com.fitconnect.entity.User; // Added
import com.fitconnect.repository.AppointmentRepository; // Added
import com.fitconnect.repository.ProfessionalRepository; // Added
import com.fitconnect.repository.ServiceRequestRepository; // Added
import com.fitconnect.repository.UserRepository; // Added

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject; // Added
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ForbiddenException; // Added
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.Optional; // Added
import java.util.concurrent.ExecutionException; // Added

@ApplicationScoped
public class ServiceRequestService {

    private static final Logger LOG = Logger.getLogger(ServiceRequestService.class);

    @Inject
    ServiceRequestRepository serviceRequestRepository; // Added

    @Inject
    UserRepository userRepository; // Added

    @Inject
    ProfessionalRepository professionalRepository; // Added

    @Inject
    AppointmentRepository appointmentRepository; // Added

    public ServiceRequest createServiceRequest(ServiceRequestInputDTO dto, String clientId) throws ExecutionException, InterruptedException { // Removed @Transactional, Long to String, added throws
        Optional<User> clientOptional = userRepository.findById(clientId);
        if (clientOptional.isEmpty()) {
            LOG.warnf("Client with ID %s not found when trying to create service request.", clientId);
            throw new NotFoundException("Client not found with ID: " + clientId);
        }
        // User client = clientOptional.get(); // Not strictly needed if only ID is stored in SR

        ServiceRequest request = new ServiceRequest();
        request.setClientId(clientId); // Set ID directly
        request.setCategory(dto.getCategory());
        request.setServiceDescription(dto.getServiceDescription());
        request.setBudget(dto.getBudget());
        // createdAt and status will be set by repository if not set here
        request.setCreatedAt(LocalDateTime.now());
        request.setStatus(ServiceRequestStatus.OPEN);


        serviceRequestRepository.save(request); // Changed from persist
        LOG.infof("Service request created with ID %s for client ID %s.", request.getId(), clientId);
        return request;
    }

    public Appointment selectProfessionalForServiceRequest(String serviceRequestId, String professionalId, String clientId) throws ExecutionException, InterruptedException { // Removed @Transactional, Longs to Strings, added throws
        Optional<ServiceRequest> srOptional = serviceRequestRepository.findById(serviceRequestId);
        if (srOptional.isEmpty()) {
            throw new NotFoundException("ServiceRequest not found with ID: " + serviceRequestId);
        }
        ServiceRequest serviceRequest = srOptional.get();

        if (!serviceRequest.getClientId().equals(clientId)) {
            LOG.warnf("Client ID %s attempted to select professional for service request %s not owned by them.", clientId, serviceRequestId);
            throw new ForbiddenException("You are not authorized to modify this service request.");
        }
        if (serviceRequest.getStatus() != ServiceRequestStatus.OPEN && serviceRequest.getStatus() != ServiceRequestStatus.MATCHED) {
            LOG.warnf("Service request %s is not in a state where a professional can be selected. Current status: %s", serviceRequestId, serviceRequest.getStatus());
            throw new IllegalStateException("Professional can only be selected for OPEN or MATCHED service requests.");
        }

        Optional<Professional> profOptional = professionalRepository.findById(professionalId);
        if (profOptional.isEmpty()) {
            throw new NotFoundException("Professional not found with ID: " + professionalId);
        }
        Professional professional = profOptional.get();

        // Optional<User> clientUserOptional = userRepository.findById(clientId); // For name in communication details
        // String clientName = clientUserOptional.map(User::getName).orElse("Client " + clientId);

        Optional<Appointment> existingAppointmentOptional = appointmentRepository.findByServiceRequestId(serviceRequestId);
        if (existingAppointmentOptional.isPresent()) {
            LOG.warnf("Client ID %s attempted to select professional for service request %s which already has an appointment %s.", clientId, serviceRequestId, existingAppointmentOptional.get().getId());
            throw new IllegalStateException("A professional has already been selected or an appointment exists for this service request.");
        }

        Appointment appointment = new Appointment();
        appointment.setClientId(serviceRequest.getClientId());
        appointment.setProfessionalId(professional.getId());
        appointment.setServiceRequestId(serviceRequest.getId());
        appointment.setStatus(AppointmentStatus.REQUESTED); // Professional needs to see this request
        appointment.setCreatedAt(LocalDateTime.now()); // Explicitly set

        // Fetch client and professional names for communication details (optional, could be done by frontend)
        String clientName = userRepository.findById(serviceRequest.getClientId()).map(User::getName).orElse("Client");
        String profName = professional.getName(); // Professional object already has name

        appointment.setCommunicationDetails(String.format("Client %s selected professional %s. Contact via details on profile.",
                                                            clientName, profName));
        appointmentRepository.save(appointment); // Changed from persist

        serviceRequest.setStatus(ServiceRequestStatus.PENDING_CONTACT);
        serviceRequest.setUpdatedAt(LocalDateTime.now()); // Explicitly set
        serviceRequestRepository.save(serviceRequest); // Changed from persist

        LOG.infof("Client ID %s selected Professional ID %s for ServiceRequest ID %s. Appointment ID %s created.", clientId, professionalId, serviceRequestId, appointment.getId());
        return appointment;
    }

    // Added methods based on original subtask description, might need DTOs for return types
    public Optional<ServiceRequest> getServiceRequestById(String id) throws ExecutionException, InterruptedException {
        return serviceRequestRepository.findById(id);
    }

    public List<ServiceRequest> getAllServiceRequestsForClient(String clientId) throws ExecutionException, InterruptedException {
        return serviceRequestRepository.findByClientId(clientId);
    }

    public List<ServiceRequest> getAllOpenServiceRequestsForProfessionals(String professionalIdToExclude) throws ExecutionException, InterruptedException {
        // Assuming professionalIdToExclude is the ID of the professional who should NOT see their own requests
        return serviceRequestRepository.findOpenByNotClientId(professionalIdToExclude);
    }

    public ServiceRequest updateServiceRequestStatus(String id, ServiceRequestStatus newStatus) throws ExecutionException, InterruptedException {
        Optional<ServiceRequest> srOptional = serviceRequestRepository.findById(id);
        if (srOptional.isEmpty()) {
            throw new NotFoundException("ServiceRequest not found with ID: " + id);
        }
        ServiceRequest serviceRequest = srOptional.get();
        serviceRequest.setStatus(newStatus);
        serviceRequest.setUpdatedAt(LocalDateTime.now()); // Explicitly set
        return serviceRequestRepository.save(serviceRequest);
    }
}
