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
import com.fitconnect.dto.ProfessionalProfileUpdateDTO; // Added

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT; // Added
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.NotFoundException; // Added for use in new methods
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/professionals")
@Produces(MediaType.APPLICATION_JSON)
public class ProfessionalResource {

    private static final Logger LOG = Logger.getLogger(ProfessionalResource.class);

    @Inject
    ProfessionalService professionalService;

    @Inject
    ProfessionalScreeningService screeningService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/register")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response registerProfessional(@MultipartForm ProfessionalRegisterRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body is missing.").build();
        }
        try {
            professionalService.registerProfessional(request);
            return Response.status(Response.Status.CREATED).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (RuntimeException e) {
             if (e.getMessage() != null && e.getMessage().startsWith("Failed to store document")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            }
            LOG.error("Error during professional registration: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred during registration: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/{id}/screen")
    public Response screenProfessional(@PathParam("id") Long id) {
        if (screeningService == null) {
             LOG.error("ProfessionalScreeningService not injected prior to screenProfessional call.");
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Screening service is not available.").build();
        }
        try {
            screeningService.screenProfessionalProfile(id);
            return Response.ok().entity("Screening process initiated for professional ID: " + id).build();
        } catch (Exception e) {
            LOG.errorf(e, "Error during manual screening trigger for professional ID: %d", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error initiating screening: " + e.getMessage())
                           .build();
        }
    }

    @GET
    @Path("/me/status")
    @RolesAllowed("PROFESSIONAL")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMyProfileStatus(@Context SecurityContext ctx) {
        if (ctx.getUserPrincipal() == null || jwt == null || jwt.getSubject() == null) {
            LOG.warn("Attempt to access /me/status without valid authentication ");
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not authenticated or JWT missing subject.").build();
        }

        String professionalIdStr = jwt.getSubject();
        Long professionalId;
        try {
            professionalId = Long.parseLong(professionalIdStr);
        } catch (NumberFormatException e) {
            LOG.errorf(e, "Invalid professional ID format in JWT subject: %s", professionalIdStr);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Invalid user identifier in token.").build();
        }

        Professional professional = Professional.findById(professionalId);
        if (professional == null) {
            LOG.warnf("Professional not found with ID %d from JWT for status check.", professionalId);
            return Response.status(Response.Status.NOT_FOUND).entity("Professional profile not found.").build();
        }

        LOG.infof("Professional ID %d retrieved profile status: %s", professionalId, professional.profileStatus);
        return Response.ok(Collections.singletonMap("status", professional.profileStatus.name())).build();
    }

    @GET
    @Path("/me/dashboard")
    @RolesAllowed("PROFESSIONAL")
    public Response getMyDashboard(@Context SecurityContext ctx) {
        String professionalIdStr = jwt.getSubject();
        Long professionalId;
        try {
            professionalId = Long.parseLong(professionalIdStr);
        } catch (NumberFormatException e) {
            LOG.warn("User ID from JWT is not a valid Long for professional dashboard: " + professionalIdStr);
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid user identifier.").build();
        }

        List<Appointment> requestedAppointments = Appointment.list("professional.id = ?1 and status = ?2",
                                                                   professionalId, AppointmentStatus.REQUESTED);

        List<AppointmentDTO> appointmentDTOs = requestedAppointments.stream()
                                                .map(AppointmentDTO::new)
                                                .collect(Collectors.toList());
        LOG.infof("Professional %d dashboard retrieved %d new requests.", professionalId, appointmentDTOs.size());
        return Response.ok(appointmentDTOs).build();
    }

    @GET
    @Path("/{id}/client-view")
    @RolesAllowed({"CLIENT", "ADMIN", "PROFESSIONAL"})
    public Response getProfessionalPublicProfile(@PathParam("id") Long professionalId) {
        Professional professional = Professional.findById(professionalId);
        // In ProfessionalService, getProfessionalById already handles NotFound.
        // Here, we also check status for public view.
        if (professional == null || professional.profileStatus != ProfileStatus.VERIFIED) {
            LOG.warnf("Attempt to view non-verified or non-existent professional profile ID %d for client view.", professionalId);
            return Response.status(Response.Status.NOT_FOUND).entity("Professional profile not found or not verified.").build();
        }
        LOG.infof("Client view requested for professional ID %d.", professionalId);
        return Response.ok(new ProfessionalPublicProfileDTO(professional)).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed("PROFESSIONAL")
    public Response getMyFullProfile(@Context SecurityContext ctx) {
        String professionalIdStr = jwt.getSubject();
        Long professionalId;
        try {
            professionalId = Long.parseLong(professionalIdStr);
        } catch (NumberFormatException e) {
            LOG.warn("User ID from JWT is not a valid Long for getMyFullProfile: " + professionalIdStr);
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid user identifier.").build();
        }

        try {
            Professional professional = professionalService.getProfessionalById(professionalId);
            return Response.ok(new ProfessionalFullProfileDTO(professional)).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/me")
    @RolesAllowed("PROFESSIONAL")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateMyProfile(ProfessionalProfileUpdateDTO updateDTO, @Context SecurityContext ctx) {
        String professionalIdStr = jwt.getSubject();
        Long professionalId;
        try {
            professionalId = Long.parseLong(professionalIdStr);
        } catch (NumberFormatException e) {
            LOG.warn("User ID from JWT is not a valid Long for updateMyProfile: " + professionalIdStr);
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
        } catch (Exception e) {
            LOG.errorf(e, "Error updating profile for professional ID %d", professionalId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating profile.").build();
        }
    }
}
