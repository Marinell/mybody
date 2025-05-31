package com.professionisti.sport.rest;

import com.professionisti.sport.model.Professional;
import com.professionisti.sport.model.enums.ScreeningStatus;
import com.professionisti.sport.service.ProfessionalScreeningService;
import com.professionisti.sport.service.UserService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
// @RolesAllowed("ROLE_ADMIN") // TODO: Uncomment when security is properly configured
public class AdminResource {

    @Inject
    ProfessionalScreeningService screeningService;

    @Inject
    UserService userService;

    @POST
    @Path("/professionals/{id}/screen")
    public Response screenProfessional(@PathParam("id") Long professionalId) {
        try {
            Professional professional = screeningService.screenProfessionalProfile(professionalId);
            // Return a more structured response, perhaps the professional object itself or a summary DTO
            return Response.ok(professional).build();
        } catch (WebApplicationException e) {
            return e.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error during screening process: " + e.getMessage()).build();
        }
    }

    public static class ScreeningUpdateRequest {
        public ScreeningStatus status;
        public String notes;
    }

    @PUT
    @Path("/professionals/{id}/status")
    public Response updateProfessionalStatus(@PathParam("id") Long professionalId, ScreeningUpdateRequest request) {
         if (request == null || request.status == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Screening status is required.").build();
        }
        try {
            Professional professional = screeningService.updateScreeningStatus(professionalId, request.status, request.notes);
            return Response.ok(professional).build();
        } catch (WebApplicationException e) {
            return e.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error updating screening status: " + e.getMessage()).build();
        }
    }
}
