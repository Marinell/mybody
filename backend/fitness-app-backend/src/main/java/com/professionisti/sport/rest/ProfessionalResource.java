package com.professionisti.sport.rest;

import com.professionisti.sport.dto.professional.ProfessionalPublicViewDTO;
import com.professionisti.sport.dto.professional.DashboardRequestViewDTO; // Added
import com.professionisti.sport.model.Professional;
import com.professionisti.sport.model.User;
import com.professionisti.sport.service.UserService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
// import jakarta.ws.rs.core.Context;
// import jakarta.ws.rs.core.SecurityContext;

import java.util.List; // Added
import java.util.Optional;

@Path("/api/professionals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfessionalResource {

    @Inject
    UserService userService;

    // @Context
    // SecurityContext securityContext;
    private String getProfessionalEmailFromContext() {
        return "test.professional@example.com"; // Placeholder
    }


    @GET
    @Path("/me/status")
    // @RolesAllowed("ROLE_PROFESSIONAL")
    public Response getMyScreeningStatus(/*@HeaderParam("Authorization") String authorizationHeader */) {
        String userEmail = getProfessionalEmailFromContext();
        Optional<User> userOpt = userService.findUserByEmail(userEmail);
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Professional)) {
             return Response.status(Response.Status.NOT_FOUND).entity("Professional profile not found.").build();
        }
        Professional professional = (Professional) userOpt.get();

        String statusInfo = String.format("{\"status\":\"%s\", \"summary\":\"%s\"}",
                                          professional.getScreeningStatus().toString(),
                                          professional.getSkillsSummary() != null ? professional.getSkillsSummary().replace("\"", "\\"") : "N/A");
        return Response.ok(statusInfo).type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/{id}/details")
    public Response getProfessionalDetails(@PathParam("id") Long professionalId) {
        try {
            ProfessionalPublicViewDTO dto = userService.getApprovedProfessionalDetails(professionalId);
            return Response.ok(dto).build();
        } catch (WebApplicationException e) { return e.getResponse(); }
        catch (Exception e) { e.printStackTrace(); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error: " + e.getMessage()).build(); }
    }

    @GET
    @Path("/me/dashboard/requests")
    // @RolesAllowed("ROLE_PROFESSIONAL")
    public Response getDashboardRequests() {
        String professionalEmail = getProfessionalEmailFromContext();
        try {
            List<DashboardRequestViewDTO> requests = userService.getDashboardRequests(professionalEmail);
            return Response.ok(requests).build();
        } catch (WebApplicationException e) { return e.getResponse(); }
        catch (Exception e) { e.printStackTrace(); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error: " + e.getMessage()).build(); }
    }
}
