package com.fitconnect.resource;

import com.fitconnect.dto.ProfessionalRegisterRequest;
import com.fitconnect.entity.Professional;
import com.fitconnect.entity.ProfileStatus;
import com.fitconnect.service.ProfessionalService;
import com.fitconnect.service.ProfessionalScreeningService;
import com.fitconnect.dto.AppointmentDTO;
import com.fitconnect.dto.ProfessionalPublicProfileDTO;
import com.fitconnect.entity.Appointment;
import com.fitconnect.entity.AppointmentStatus;
import com.fitconnect.dto.ProfessionalFullProfileDTO; // Added
import com.fitconnect.dto.ProfessionalProfileUpdateDTO;
import com.fitconnect.repository.AppointmentRepository; // Added
import io.quarkus.security.identity.SecurityIdentity; // Added


import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
// import jakarta.ws.rs.POST; // Commented out /register
// import jakarta.ws.rs.PUT; // Already present
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
// import jakarta.ws.rs.core.Context; // Commented out
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
// import jakarta.ws.rs.core.SecurityContext; // Commented out
import jakarta.ws.rs.NotFoundException;
// import org.eclipse.microprofile.jwt.JsonWebToken; // Commented out
// import org.jboss.resteasy.reactive.MultipartForm; // Commented out /register
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Path("/api/professionals")
@Produces(MediaType.APPLICATION_JSON)
public class ProfessionalResource {

    private static final Logger LOG = Logger.getLogger(ProfessionalResource.class);

    @Inject
    ProfessionalService professionalService;

    @Inject
    ProfessionalScreeningService screeningService; // Keep if /screen endpoint is maintained

    @Inject
    AppointmentRepository appointmentRepository; // Added

    @Inject
    SecurityIdentity securityIdentity; // Added

    /* Commenting out /register as primary flow is via AuthResource.initializeUser
    @POST
    @Path("/register")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response registerProfessional(@MultipartForm ProfessionalRegisterRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body is missing.").build();
        }
        try {
            Professional professional = professionalService.registerProfessional(request);
            ProfessionalFullProfileDTO dto = new ProfessionalFullProfileDTO();
            // Ensure professional.getId() is not null if it's used from FirestoreEntity
            dto.setId(professional.getId() != null ? professional.getId().toString() : null);
            dto.setName(professional.getName());
            dto.setEmail(professional.getEmail());
            return Response.status(Response.Status.CREATED).entity(dto).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) { // Catch broader exceptions like ExecutionException, InterruptedException
             if (e.getMessage() != null && e.getMessage().contains("Failed to store document")) { // Example check
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            }
            LOG.error("Error during professional registration: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred during registration: " + e.getMessage()).build();
        }
    }*/

    @POST
    @Path("/{id}/screen")
    // @RolesAllowed("ADMIN") // Consider adding admin role for this
    public Response screenProfessional(@PathParam("id") String id) { // Changed Long to String
        if (screeningService == null) {
             LOG.error("ProfessionalScreeningService not injected prior to screenProfessional call.");
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Screening service is not available.").build();
        }
        try {
            screeningService.screenProfessionalProfile(id); // Changed from Long.parseLong(id)
            return Response.ok().entity("Screening process initiated for professional ID: " + id).build();
        } catch (Exception e) {
            LOG.errorf(e, "Error during manual screening trigger for professional ID: %s", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error initiating screening: " + e.getMessage())
                           .build();
        }
    }

    @GET
    @Path("/me/status")
    @RolesAllowed("PROFESSIONAL")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMyProfileStatus() { // Removed @Context SecurityContext
        String professionalId = securityIdentity.getPrincipal().getName();
        if (professionalId == null || professionalId.isEmpty()) {
            LOG.warn("Attempt to access /me/status without valid authentication (UID missing).");
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not authenticated or UID missing.").build();
        }

        try {
            Professional professional = professionalService.getProfessionalById(professionalId);
            LOG.infof("Professional ID %s retrieved profile status: %s", professionalId, professional.getProfileStatus());
            return Response.ok(Collections.singletonMap("status", professional.getProfileStatus().name())).build();
        } catch (NotFoundException e) {
            LOG.warnf("Professional not found with ID %s from security context for status check.", professionalId);
            return Response.status(Response.Status.NOT_FOUND).entity("Professional profile not found.").build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error fetching profile status for professional ID %s", professionalId);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving profile status.").build();
        }
    }

    @GET
    @Path("/me/dashboard")
    @RolesAllowed("PROFESSIONAL")
    public Response getMyDashboard() { // Removed @Context SecurityContext
        String professionalId = securityIdentity.getPrincipal().getName();
        if (professionalId == null || professionalId.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid user identifier.").build();
        }

        try {
            List<Appointment> requestedAppointments = appointmentRepository.findByProfessionalIdAndStatus(professionalId, AppointmentStatus.REQUESTED);
            List<AppointmentDTO> appointmentDTOs = requestedAppointments.stream()
                                                    .map(AppointmentDTO::new)
                                                    .collect(Collectors.toList());
            LOG.infof("Professional %s dashboard retrieved %d new requests.", professionalId, appointmentDTOs.size());
            return Response.ok(appointmentDTOs).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error fetching dashboard for professional ID %s", professionalId);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving dashboard data.").build();
        }
    }

    @GET
    @Path("/{id}/client-view")
    @RolesAllowed({"CLIENT", "ADMIN", "PROFESSIONAL"}) // Or @PermitAll if public and verified
    public Response getProfessionalPublicProfile(@PathParam("id") String professionalId) { // Changed Long to String
        try {
            Professional professional = professionalService.getProfessionalById(professionalId);
            if (professional.getProfileStatus() != ProfileStatus.VERIFIED) {
                LOG.warnf("Attempt to view non-verified professional profile ID %s for client view.", professionalId);
                return Response.status(Response.Status.NOT_FOUND).entity("Professional profile not found or not verified.").build();
            }
            LOG.infof("Client view requested for professional ID %s.", professionalId);
            return Response.ok(new ProfessionalPublicProfileDTO(professional)).build();
        } catch (NotFoundException e) {
             return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error fetching public profile for professional ID %s", professionalId);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving professional profile.").build();
        }
    }

    @GET
    @Path("/me")
    @RolesAllowed("PROFESSIONAL")
    public Response getMyFullProfile() { // Removed @Context SecurityContext
        String professionalId = securityIdentity.getPrincipal().getName();
         if (professionalId == null || professionalId.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid user identifier.").build();
        }
        try {
            Professional professional = professionalService.getProfessionalById(professionalId);
            return Response.ok(new ProfessionalFullProfileDTO(professional)).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error fetching full profile for professional ID %s", professionalId);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving full profile.").build();
        }
    }

    @PUT
    @Path("/me")
    @RolesAllowed("PROFESSIONAL")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateMyProfile(ProfessionalProfileUpdateDTO updateDTO) { // Removed @Context SecurityContext
        String professionalId = securityIdentity.getPrincipal().getName();
        if (professionalId == null || professionalId.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid user identifier.").build();
        }

        if (updateDTO == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body is missing.").build();
        }

        try {
            Professional updatedProfessional = professionalService.updateProfessionalProfile(professionalId, updateDTO);
            return Response.ok(new ProfessionalFullProfileDTO(updatedProfessional)).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error updating profile for professional ID %s", professionalId);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating profile.").build();
        } catch (Exception e) { // General catch for other unexpected errors from service
            LOG.errorf(e, "Unexpected error updating profile for professional ID %s", professionalId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred while updating profile.").build();
        }
    }
}
