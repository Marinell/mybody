package com.fitconnect.resource;

import com.fitconnect.dto.ServiceRequestInputDTO;
import com.fitconnect.entity.ServiceRequest;
import com.fitconnect.service.ServiceRequestService;
import com.fitconnect.dto.MatchResponseDTO;
import com.fitconnect.service.MatchingService;
import com.fitconnect.dto.ClientSelectProfessionalRequestDTO;
import com.fitconnect.dto.AppointmentDTO;
import com.fitconnect.entity.Appointment;
import io.quarkus.security.identity.SecurityIdentity; // Added


import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
// import jakarta.ws.rs.core.Context; // No longer using @Context SecurityContext
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
// import jakarta.ws.rs.core.SecurityContext; // No longer using @Context SecurityContext
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ForbiddenException; // Added for explicit ForbiddenException handling


// import org.eclipse.microprofile.jwt.JsonWebToken; // Removed
import org.jboss.logging.Logger;

import java.util.Optional; // Added
import java.util.concurrent.ExecutionException; // Added

@Path("/api/service-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceRequestResource {

    private static final Logger LOG = Logger.getLogger(ServiceRequestResource.class);

    @Inject
    ServiceRequestService serviceRequestService;

    @Inject
    MatchingService matchingService;

    @Inject
    SecurityIdentity securityIdentity; // Added

    @POST
    @RolesAllowed("CLIENT")
    public Response submitServiceRequest(ServiceRequestInputDTO requestDTO) { // Removed @Context SecurityContext
        if (requestDTO == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Request body is missing.")
                           .build();
        }

        String clientId = securityIdentity.getPrincipal().getName();
        if (clientId == null || clientId.trim().isEmpty()) {
            LOG.warn("Client ID missing from security context when submitting service request.");
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("User identifier not found.")
                           .build();
        }

        try {
            ServiceRequest createdRequest = serviceRequestService.createServiceRequest(requestDTO, clientId);
            return Response.status(Response.Status.CREATED).entity(createdRequest).build();
        } catch (NotFoundException e) {
            LOG.warnf("Failed to create service request for client ID %s: %s", clientId, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error creating service request for client ID %s", clientId);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing request.").build();
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error while creating service request for client ID %s", clientId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An unexpected error occurred while creating the service request.")
                           .build();
        }
    }

    @GET
    @Path("/{id}/matches")
    @RolesAllowed({"CLIENT", "ADMIN"})
    public Response getMatchesForServiceRequest(@PathParam("id") String serviceRequestId) { // Changed Long to String
        String currentUserId = securityIdentity.getPrincipal().getName();
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User identifier not found.").build();
        }

        try {
            Optional<ServiceRequest> srOptional = serviceRequestService.getServiceRequestById(serviceRequestId);
            if (srOptional.isEmpty()) {
                 return Response.status(Response.Status.NOT_FOUND).entity("Service request not found.").build();
            }
            ServiceRequest sr = srOptional.get();

            boolean isAdmin = securityIdentity.hasRole("ADMIN");
            if (!isAdmin && (sr.getClientId() == null || !sr.getClientId().equals(currentUserId))) {
                LOG.warnf("User %s (roles %s) attempted to access matches for service request %s owned by user %s",
                    currentUserId, securityIdentity.getRoles(), serviceRequestId, sr.getClientId());
                return Response.status(Response.Status.FORBIDDEN).entity("You are not authorized to view matches for this service request.").build();
            }

            MatchResponseDTO matches = matchingService.findMatchesForServiceRequest(serviceRequestId);
            return Response.ok(matches).build();
        } catch (NotFoundException e) {
            LOG.warnf("Error finding matches for service request ID %s: %s", serviceRequestId, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error finding matches for service request ID %s", serviceRequestId);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing request.").build();
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error finding matches for service request ID %s", serviceRequestId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An unexpected error occurred while finding matches.")
                           .build();
        }
    }

    @POST
    @Path("/{serviceRequestId}/select-professional")
    @RolesAllowed("CLIENT")
    public Response selectProfessional(
            @PathParam("serviceRequestId") String serviceRequestId, // Changed Long to String
            ClientSelectProfessionalRequestDTO selectionDTO) { // Removed @Context SecurityContext

        String clientId = securityIdentity.getPrincipal().getName();
        if (clientId == null || clientId.trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User identifier not found.").build();
        }

        String professionalId = selectionDTO.getProfessionalId();
        if (professionalId == null || professionalId.isEmpty()) { // Combined null/empty check
             return Response.status(Response.Status.BAD_REQUEST).entity("Professional ID is missing in the request.").build();
        }

        // Removed redundant null check for selectionDTO.getProfessionalId() as it's covered by professionalId check

        try {
            Appointment appointment = serviceRequestService.selectProfessionalForServiceRequest(
                serviceRequestId, professionalId, clientId);
            return Response.ok(new AppointmentDTO(appointment)).build(); // Return Appointment DTO
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error selecting professional for service request %s", serviceRequestId);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing request.").build();
        } catch (Exception e) {
            LOG.errorf(e, "Error selecting professional for service request %s", serviceRequestId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error selecting professional.").build();
        }
    }

    @GET
    @Path("/client/me")
    @RolesAllowed("CLIENT")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMyServiceRequests() {
        String clientId = securityIdentity.getPrincipal().getName();
        if (clientId == null || clientId.trim().isEmpty()) {
            LOG.warn("Client ID missing from security context when fetching their service requests.");
            return Response.status(Response.Status.UNAUTHORIZED).entity("User identifier not found.").build();
        }
        try {
            List<ServiceRequest> requests = serviceRequestService.getAllServiceRequestsForClient(clientId);
            return Response.ok(requests).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error fetching service requests for client %s", clientId);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error fetching service requests.").build();
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error fetching service requests for client %s", clientId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred.").build();
        }
    }

    @GET
    @Path("/professional/open")
    @RolesAllowed("PROFESSIONAL")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOpenServiceRequestsForProfessional() {
        String professionalId = securityIdentity.getPrincipal().getName();
         if (professionalId == null || professionalId.trim().isEmpty()) {
            LOG.warn("Professional ID missing from security context when fetching open service requests.");
            return Response.status(Response.Status.UNAUTHORIZED).entity("User identifier not found.").build();
        }
        try {
            List<ServiceRequest> requests = serviceRequestService.getAllOpenServiceRequestsForProfessionals(professionalId);
            return Response.ok(requests).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error fetching open service requests for professional %s", professionalId);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error fetching open service requests.").build();
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error fetching open service requests for professional %s", professionalId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred.").build();
        }
    }
}
