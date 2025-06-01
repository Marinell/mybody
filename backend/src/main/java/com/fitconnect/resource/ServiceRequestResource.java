package com.fitconnect.resource;

import com.fitconnect.dto.ServiceRequestInputDTO;
import com.fitconnect.entity.ServiceRequest;
import com.fitconnect.service.ServiceRequestService;
import com.fitconnect.dto.MatchResponseDTO;
import com.fitconnect.service.MatchingService;
import com.fitconnect.dto.ClientSelectProfessionalRequestDTO; // Added
import com.fitconnect.dto.AppointmentDTO; // Added
import com.fitconnect.entity.Appointment; // Added


import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.NotFoundException;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

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
    JsonWebToken jwt; // To get the client's ID from the token

    @POST
    @RolesAllowed("CLIENT")
    public Response submitServiceRequest(ServiceRequestInputDTO requestDTO, @Context SecurityContext ctx) {
        if (requestDTO == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Request body is missing.")
                           .build();
        }

        String clientIdStr = jwt.getSubject();
        if (clientIdStr == null || clientIdStr.trim().isEmpty()) {
            LOG.warn("Client ID missing from JWT subject when submitting service request.");
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("User identifier not found in token.")
                           .build();
        }

        Long clientId;
        try {
            clientId = Long.parseLong(clientIdStr);
        } catch (NumberFormatException e) {
            LOG.errorf(e, "Invalid client ID format in JWT subject: %s", clientIdStr);
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Invalid user identifier format in token.")
                           .build();
        }

        try {
            ServiceRequest createdRequest = serviceRequestService.createServiceRequest(requestDTO, clientId);
            return Response.status(Response.Status.CREATED).entity(createdRequest).build();
        } catch (NotFoundException e) {
            LOG.warnf("Failed to create service request for client ID %d: %s", clientId, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error while creating service request for client ID %d", clientId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An unexpected error occurred while creating the service request.")
                           .build();
        }
    }

    @GET
    @Path("/{id}/matches")
    @RolesAllowed({"CLIENT", "ADMIN"})
    public Response getMatchesForServiceRequest(@PathParam("id") Long serviceRequestId, @Context SecurityContext ctx) {
        String currentUserIdStr = jwt.getSubject();
        Long currentUserId;
        try {
            currentUserId = Long.parseLong(currentUserIdStr);
        } catch (NumberFormatException e) {
            LOG.warn("User ID from JWT is not a valid Long: " + currentUserIdStr);
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid user identifier.").build();
        }

        ServiceRequest sr = ServiceRequest.findById(serviceRequestId);
        if (sr == null) {
             return Response.status(Response.Status.NOT_FOUND).entity("Service request not found.").build();
        }

        if (!ctx.isUserInRole("ADMIN") && (sr.getClient() == null || !sr.getClient().id.equals(currentUserId))) {
            LOG.warnf("User %d (role %s) attempted to access matches for service request %d owned by user %s (client object: %s)",
                currentUserId, jwt.getGroups(), serviceRequestId, sr.getClient() !=null ? sr.getClient().id : "null", sr.getClient());
            return Response.status(Response.Status.FORBIDDEN).entity("You are not authorized to view matches for this service request.").build();
        }

        try {
            MatchResponseDTO matches = matchingService.findMatchesForServiceRequest(serviceRequestId);
            return Response.ok(matches).build();
        } catch (NotFoundException e) {
            LOG.warnf("Error finding matches for service request ID %d: %s", serviceRequestId, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error finding matches for service request ID %d", serviceRequestId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An unexpected error occurred while finding matches.")
                           .build();
        }
    }

    @POST
    @Path("/{serviceRequestId}/select-professional")
    @RolesAllowed("CLIENT")
    public Response selectProfessional(
            @PathParam("serviceRequestId") Long serviceRequestId,
            ClientSelectProfessionalRequestDTO selectionDTO,
            @Context SecurityContext ctx) {

        String clientIdStr = jwt.getSubject();
        Long clientId;
        try {
            clientId = Long.parseLong(clientIdStr);
        } catch (NumberFormatException e) {
            LOG.warn("User ID from JWT is not a valid Long: " + clientIdStr);
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid user identifier.").build();
        }

        if (selectionDTO == null || selectionDTO.getProfessionalId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Professional ID is missing in the request.").build();
        }

        try {
            Appointment appointment = serviceRequestService.selectProfessionalForServiceRequest(
                serviceRequestId, selectionDTO.getProfessionalId(), clientId);
            return Response.ok(new AppointmentDTO(appointment)).build(); // Return Appointment DTO
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (jakarta.ws.rs.ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build(); // 409 Conflict for state issues
        } catch (Exception e) {
            LOG.errorf(e, "Error selecting professional for service request %d", serviceRequestId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error selecting professional.").build();
        }
    }
}
